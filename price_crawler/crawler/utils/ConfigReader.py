import logging
import configparser
import os
from typing import Optional

class ConfigReader:

    def __init__(self, config_file: str = "config.prod.ini") -> None:
        """
        Initializes the configuration manager for the web crawler. This includes setting up logging, defining
        mandatory configuration settings, and reading the configuration file.

        :param config_file: The path to the configuration file. Defaults to "config.prod.ini".
        :type config_file: str
        """
        # setup logging
        self.logger = logging.getLogger('web_crawl_logger')

        self.mandatory_settings = {
            "General": {
                "crawler_name": str,
                "robots_crawl_timeout": int,
            },
            "Kafka": {
                "bootstrap_servers": str,
                "kafka_topic": str,
                "kafka_response_topic": str,
            },
            "Crawler": {
                "robots_txt_cache_time": int,
                "gemini_api_key": str,
            },
            "Selenium": {
                "page_load_timeout": int,
            }
        }

        # read config file
        self.config = self._read_config(config_file)


    def _read_config(self, config_file: str) -> configparser.ConfigParser:
        """
        Reads and validates the configuration file. Constructs the full path to the config file, checks for its
        existence, and loads it into a `ConfigParser` object. It also checks for the mandatory settings in the config.

        :param config_file: The name of the configuration file to be read.
        :type config_file: str
        :return: The parsed configuration file as a `ConfigParser` object.
        :rtype: configparser.ConfigParser
        :raises FileNotFoundError: If the config file cannot be found.
        :raises configparser.ParsingError: If the config file does not contain all mandatory settings.
        :raises Exception: If there is an error reading the config file.
        """
        try:
            # e.g. /home/phil/coding/24ws-ase-pr-qse-02/crawler/utils
            utils_filepath = os.path.dirname(os.path.abspath(__file__))
            # e.g. /home/phil/coding/24ws-ase-pr-qse-02/crawler
            crawler_filepath = os.path.dirname(utils_filepath)

            project_root = os.path.dirname(crawler_filepath)

            # e.g. /home/phil/coding/24ws-ase-pr-qse-02/crawler/config.ini
            config_filepath = os.path.join(project_root, config_file)
        except Exception as e:
            self.logger.error("Error during creating the path to the config file. Exception: " + str(e))
            raise FileNotFoundError("Filepath to config file could not be found")

        # check if the config file exists
        if not os.path.isfile(config_filepath):
            # config file not found
            self.logger.error("Config file not found! File not found: " + str(config_filepath) +
                           "; current path: " + str(os.path.dirname(os.path.realpath(__file__))))
            raise FileNotFoundError("Config file not found! File not found: " + str(config_filepath))

        # read the config
        config = configparser.ConfigParser()
        try:
            config.read(config_filepath)
        except Exception as e:
            self.logger.error("Failed to read config file. Error: " + str(e))
            raise Exception(str(e))

        # check mandatory config settings
        if not self.__validate_config(config):
            # some checks failed
            raise configparser.ParsingError("config.ini")

        # return the configparser object
        return config


    def __validate_config(self, config: configparser) -> bool:
        """
        Validates the configuration file by checking for the presence of required sections and keys,
        and verifying that their values have the correct type (e.g., int, float, str).

        :param config: The configuration object to validate.
        :type config: configparser.ConfigParser
        :return: True if the configuration file is valid, False otherwise.
        :rtype: bool
        """
        ret: bool = True

        for section, settings in self.mandatory_settings.items():
            if section not in config:
                self.logger.error("Missing section in config file: Section: " + str(section))
                ret = False
                continue

            for key, expected_type in settings.items():
                if key not in config[section]:
                    self.logger.error("Missing key: " + str(key) + " in section " + str(section))
                    ret = False
                else:
                    value = config[section][key]
                    try:
                        # Validate the type by attempting a cast
                        if expected_type == int:
                            int(value)
                        elif expected_type == float:
                            float(value)
                        elif expected_type == str:
                            str(value)
                    except ValueError:
                        ret = False
                        self.logger.error(f"Invalid type for key: {key} in section [{section}]. Expected {expected_type.__name__}, got value: {value}")

        return ret


    def get(self, section: str, option: str) -> Optional[str | int | float]:
        """
        Retrieves the value of a specific option from a given section in the configuration file.
        If the value cannot be retrieved due to an error, logs the error and returns None.

        :param section: The section in the configuration file from which to retrieve the value.
        :type section: str
        :param option: The option whose value is to be retrieved.
        :type option: str
        :return: The value of the specified option, which could be a string, integer, or float,
                 or None if the value could not be retrieved.
        :rtype: Optional[str | int | float]
        """
        try:
            return self.config[section][option]
        except Exception as e:
            self.logger.error(f"Failed to get value from section [{section}]. Exception: " + str(e))
            return None