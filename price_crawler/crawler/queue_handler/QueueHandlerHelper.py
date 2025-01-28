import logging
from crawler.utils.UtilsExceptions import InvalidLoggerObjectException, InvalidConfigObjectException
from crawler.utils.ConfigReader import ConfigReader

class QueueHandlerHelper:

    @staticmethod
    def validate_logging_object(logger: logging) -> logging.Logger:
        if not isinstance(logger, logging.Logger):
            raise InvalidLoggerObjectException("Expected logger to be an instance of logging.Logger")

        return logger

    @staticmethod
    def validate_config_object(config: ConfigReader) -> ConfigReader:
        if not isinstance(config, ConfigReader):
            raise InvalidConfigObjectException("Expected config to be an instance of ConfigReader")

        return config