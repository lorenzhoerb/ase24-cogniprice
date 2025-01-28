from logging.handlers import RotatingFileHandler

import logging
import os

def setup_logging(log_level: int = logging.DEBUG) -> logging.Logger:
    """
    Sets up and configures logging for the application. This includes creating a custom logger, setting up log
    rotation for file handling, and streaming logs to the console.

    :param log_level: The logging level to use (e.g., logging.DEBUG, logging.INFO, etc.).
    :type log_level: int
    :return: A configured logger instance for the web crawler.
    :rtype: logging.Logger
    """
    log_file = 'logs/web_crawler.log'

    # create logs folder if it does not exist
    if not os.path.isdir('logs'):
        try:
            os.mkdir('logs')
        except Exception as e:
            print("CRITICAL: Could not create logs directory. Error: " + str(e))
            exit()

    # delete old log file if one exists
    if os.path.exists(log_file):
        try:
            os.remove(log_file)
        except OSError:
            print("Error removing log file: file is a directory.")
        except Exception as e:
            print("Error removing log file: " + str(e))

    # create a new custom logger with the name "web_crawl_logger"
    logger = logging.getLogger('web_crawl_logger')
    logger.setLevel(log_level)

    # Create a formatter and add it to the handler
    formatter = logging.Formatter('%(asctime)s - %(module)s: %(funcName)s - %(levelname)s: %(message)s')

    # FILE HANDLER
    # Create a RotatingFileHandler to handle log file rotation
    # if the logfile exceeds 3Mb then it will be renamed to web_crawler.log.1
    file_handler = RotatingFileHandler(log_file, maxBytes=3 * 1024 * 1024, backupCount=3)
    file_handler.setFormatter(formatter)
    # add the file handler to the logger
    logger.addHandler(file_handler)

    # CONSOLE HANDLER
    console_handler = logging.StreamHandler()
    #console_handler.setLevel(log_level)
    console_handler.setFormatter(formatter)
    # add the console handler to the logger
    logger.addHandler(console_handler)

    return logger