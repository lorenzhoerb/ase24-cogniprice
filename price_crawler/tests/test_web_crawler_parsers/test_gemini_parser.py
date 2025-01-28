from crawler.web_crawler.parsers.GeminiParser import GeminiParser
from crawler.utils.LoggingUtil import setup_logging
from crawler.utils.ConfigReader import ConfigReader
import os
import pytest
import logging
from unittest.mock import patch, MagicMock


class TestGeminiParser:

    @pytest.fixture
    def config_object(self):
        """Provides a mocked ConfigReader with a dummy API key."""
        config = ConfigReader("config.dev.ini")
        config.get = MagicMock(return_value="mock_gemini_api_key")
        return config

    @pytest.fixture
    def logger_object(self):
        """Provides a logger object."""
        return setup_logging(log_level=logging.INFO)

    @pytest.fixture
    def mock_genai(self):
        """Mocks the Gemini API library."""
        with patch("crawler.web_crawler.parsers.GeminiParser.genai") as mock_genai:
            yield mock_genai

    def test_gemini_parser(self, config_object, logger_object, mock_genai):
        html_directory = str(os.getcwd()) + "/tests/test_platform_identifier/htmls/gemini"

        # Mock Gemini API behavior
        mock_model = MagicMock()
        mock_model.generate_content.return_value.text = '{"Price": 123.45, "Currency": "USD"}'
        mock_genai.GenerativeModel.return_value = mock_model

        # List all files in the HTML directory
        files = [f for f in os.listdir(html_directory) if os.path.isfile(os.path.join(html_directory, f))]

        for file_name in files:
            file_path = os.path.join(html_directory, file_name)
            with open(file_path, "r", encoding="utf-8") as file:
                html_page = file.read()

            gparser = GeminiParser(logger_object, config_object, html_page)

            try:
                price = gparser.get_price()
                if not price:
                    pytest.fail(f"Could not extract PRICE from file: {file_name}")
            except Exception as e:
                pytest.fail(f"Could not extract PRICE from file: {file_name}. Error: {e}")
                exit()

            try:
                currency = gparser.get_currency()
                if not currency:
                    pytest.fail(f"Could not extract CURRENCY from file: {file_name}")
            except Exception as e:
                pytest.fail(f"Could not extract CURRENCY from file: {file_name}. Error: {e}")
                exit()

            # Ensure the mocked API was called correctly
            prompt = (
                "Extract the price and currency from the following HTML content. "
                "Respond in this exact JSON format: {\"Price\": float, \"Currency\": str}. "
                f"HTML Content: {html_page}"
            )
            mock_model.generate_content.assert_called_with(prompt)
