import pytest
from unittest import mock
import logging
from crawler.queue_handler import KafkaClient, QueueHandlerHelper
from crawler.utils.LoggingUtil import setup_logging
from crawler.utils.ConfigReader import ConfigReader
from crawler.utils.UtilsExceptions import *

@pytest.mark.parametrize("client_class", [KafkaClient])
class TestKafkaClientInitialization:

    @pytest.fixture
    def config_object(self):
        return ConfigReader("config.dev.ini")

    @pytest.fixture
    def logger_object(self):
        return setup_logging(log_level=logging.INFO)

    # Test initialization of KafkaClient
    def test_valid_inputs(self, client_class, logger_object, config_object):
        logger = logger_object
        config = config_object

        client = client_class(logger, config)


    def test_invalid_logger_input(self, client_class, logger_object, config_object):
        logger = None
        config = config_object

        with pytest.raises(InvalidLoggerObjectException, match="INVALID_LOGGER_OBJECT"):
            client = client_class(logger, config)

    def test_invalid_config_input(self, client_class, logger_object, config_object):
        logger = logger_object
        config = None

        with pytest.raises(InvalidConfigObjectException, match="INVALID_CONFIG_OBJECT"):
            client = client_class(logger, config)