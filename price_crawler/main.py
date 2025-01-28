from crawler.web_crawler.DataFetcher import DataFetcher
from crawler.web_crawler.PlatformIdentifier import PlatformIdentifier
from crawler.queue_handler import KafkaClient, KafkaProducer
from crawler.utils import ConfigReader, RobotsChecker
from crawler.utils.LoggingUtil import setup_logging
from crawler.web_crawler.CrawlerExceptions import *
from crawler.queue_handler.IMessageQueueClient import CrawlJobRequest
from crawler.web_crawler.parsers.GeminiParser import GeminiParser
from crawler.web_crawler.parsers.IParser import IParser

import logging
import time
import requests
import configparser
import os

def _calc_process_time(start_time: float) -> int:
    """
    Calculates the elapsed time in seconds since the given start time.

    :param start_time: The start time from which the elapsed time will be calculated.
    :type start_time: float
    :return: The elapsed time in seconds.
    :rtype: int
    """
    return int(time.time() - start_time)


def main():
    # LOGGING
    logger = setup_logging(log_level=logging.INFO)
    logger.info("Starting crawler.")

    # CONFIG
    try:
        env = os.getenv('ENV', 'dev')
        logger.info(f'Using {env} config')
        config_file = f'config.{env}.ini'
        config = ConfigReader(config_file)
    except FileNotFoundError:
        logger.critical("Could not find config.ini file.")
        exit()
    except configparser.ParsingError:
        logger.critical("Could not parse config.ini file.")
        exit()
    except Exception as e:
        logger.critical("General Exception when reading config.ini file. Error: " + str(e))
        exit()


    # KAFKA
    message_queue: KafkaClient = KafkaClient(logger, config)
    producer: KafkaProducer = KafkaProducer(logger, config)

    # KAFKA CONNECT to the kafka broker
    if not message_queue.connect():
        try_count: int = 1
        while not message_queue.test_connection():
            # Connection to kafka broker was unsuccessful
            # per connection attempt there is a timeout of 6 seconds -> per round 6 seconds delay inherently
            # first 10 try: 6*10 = 60 seconds; second 12 trys: 15*12 = 180 seconds
            #   max wait time before error (22 trys): 240 seconds (=4 minutes)

            try_count += 1
            logger.warning("Could not connect to kafka broker. Reconnecting.. Try: " + str(try_count))

            # abort connection attempts to kafka broker after 22 trys
            if try_count > 21:
                logger.critical("Kafka connection failed after " + str(try_count) + " tries.")
                exit()
            # increase timespan between connection attempts after 10 trys
            if try_count > 9:
                # raises wait time to 15 seconds in sum
                time.sleep(9)

    # KAFKA SUBSCRIBE to the topic in the config file
    if not message_queue.subscribe():
        logger.critical("Kafka could not subscribe to queue.")
        exit()

    # setup needed objects
    robots_helper: RobotsChecker = RobotsChecker(config)
    platform_identifier: PlatformIdentifier = PlatformIdentifier(logger, config)
    try:
        data_fetcher: DataFetcher = DataFetcher(logger, config)
    except Exception as e:
        # could not initialize chrome webdriver for data fetching
        exit()

    # loop over values in the kafka pipline
    while True:
        # reset local variables
        is_allowed: bool = False
        html_page: str = ""
        site_parser: IParser = None
        price: float = None
        currency: str = None
        error_msg: str = ""


        # CONSUME MESSAGE
        try:
            crawl_job_object: CrawlJobRequest = message_queue.get_next_job()
        except Exception as e:
            logger.error("Unable to get next job from kafka queue" + str(e))
            continue

        print("Got job with productID: " + str(crawl_job_object.job_id.product_id) + " url: " + str(
            crawl_job_object.product_url))
        # measure time for processing: start
        start_time: float = time.time()


        # PROCESS MESSAGE

        # validate url
        validated_url = robots_helper.validate_url(crawl_job_object.product_url)
        if validated_url is None:
            error_msg = "Could not validate the URL: " + str(crawl_job_object.product_url)
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue

        # parse robots.txt from url
        try:
            is_allowed = robots_helper.check_crawl_allowance(validated_url)
        except URLParseException as e:
            error_msg = ("Robots.txt - Crawl allowance. Could not parse the URL: " + str(crawl_job_object.product_url)
                         + " Exception: " + str(e))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue
        except requests.exceptions.MissingSchema as e:
            error_msg = ("Robots.txt - Crawl allowance. MissingSchema in the URL: " + str(crawl_job_object.product_url)
                         + " Exception: " + str(e))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue
        except requests.ConnectionError as e:
            error_msg = ("Robots.txt - Crawl allowance. ConnectionError for the URL: " + str(crawl_job_object.product_url)
                         + " Exception: " + str(e))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue
        except requests.TooManyRedirects as e:
            error_msg = ("Robots.txt - Crawl allowance. TooManyRedirects for the URL: " + str(crawl_job_object.product_url)
                         + " Exception: " + str(e))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue
        except requests.Timeout as e:
            error_msg = ("Robots.txt - Crawl allowance. Timeout for the URL: " + str(crawl_job_object.product_url)
                         + " Exception: " + str(e))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue
        except RobotParseException as e:
            error_msg = ("Robots.txt - Crawl allowance. RobotParseException for the URL: " + str(crawl_job_object.product_url)
                         + " Exception: " + str(e))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue
        except Exception as e:
            error_msg = ("Robots.txt - Crawl allowance. General Exception for the URL: " + str(crawl_job_object.product_url)
                         + " Exception: " + str(e))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue

        # FETCH HTML

        if is_allowed:
            # ALLOWED to crawl
            try:
                html_page = data_fetcher.fetch_data(validated_url)
            except Exception as e:
                error_msg = ("Could not download html for page. Url: " + str(validated_url)
                             + " Exception: " + str(e))
                logger.error(error_msg)
                producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
                continue
        else:
            # DISALLOWED to crawl
            error_msg = ("Robots.txt does not allow the crawling of the site. Url: " + str(validated_url))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue


        # IDENTIFY PARSER
        try:
            site_parser = platform_identifier.get_correct_parser(html_page)
        except Exception as e:
            error_msg = ("Could not parse html for page. Url: " + str(validated_url))
            logger.error(error_msg)
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)
            continue

        # specific parser for site found
        if site_parser:
            # EXTRACT PRICE
            try:
                price = site_parser.get_price()
            except Exception as e:
                # Use generic crawler
                logger.error("Parser could not extract the price. Url: " + str(validated_url))
                # price still set to None -> generic crawler

            # EXTRACT CURRENCY
            try:
                currency = site_parser.get_currency()
            except Exception as e:
                # Use generic crawler
                logger.error("Parser could not extract the currency. Url: " + str(validated_url))
                # currency still set to None -> generic crawler

            # check if parser was successful
            if price and currency:
                # all good we can send back

                # SEND TO BACKEND
                producer.send_success(crawl_job_object.job_id, price, currency, _calc_process_time(start_time))
                logger.info("Successfully crawled website. Url: " + str(validated_url) + " Price: " + str(price)
                            + " Currency: " + str(currency))
                continue


        # GENERIC PARSER
        # If no parser was found or the parser could not extract the price or currency

        logger.info("No fitting parser found. Falling back to generic one")

        generic_parser = GeminiParser(logger,config, html_page)
        price = generic_parser.get_price()
        currency = generic_parser.get_currency()

        if price and currency:
            # all good we can send back
            # SEND TO BACKEND
            producer.send_success(crawl_job_object.job_id, price, currency, _calc_process_time(start_time))
            logger.info("Successfully crawled website. Url: " + str(validated_url) + " Price: " + str(price)
                        + " Currency: " + str(currency))
        else:
            # SEND TO BACKEND
            logger.warning("Generic Crawler could not extract price or currency. Failing job.")
            producer.send_failure(crawl_job_object.job_id, _calc_process_time(start_time), error_msg)


if __name__ == "__main__":
    main()