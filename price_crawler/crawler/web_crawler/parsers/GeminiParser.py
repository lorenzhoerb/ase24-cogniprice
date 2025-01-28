import json
from typing import Optional, Tuple
import logging
import google.generativeai as genai

from crawler.utils import ConfigReader
from crawler.web_crawler.parsers.IParser import IParser

class GeminiParser(IParser):
    """
    A parser that extracts price and currency information from HTML content
    using the Gemini API.

    This class handles the processing of HTML content, sends it to the Gemini API,
    and extracts the price and currency from the API response. It is designed to be
    initialized with a logger, configuration reader, and raw HTML content.
    """

    def __init__(self, logger: logging.Logger, config: ConfigReader, html_page: str) -> None:
        """
        Initializes the GeminiParser with the provided logger, configuration, and HTML content.

        :param logger: The logger object used for logging messages and errors.
        :type logger: logging.Logger
        :param config: The configuration reader used for accessing settings.
        :type config: ConfigReader
        :param html_page: The raw HTML content from which the price and currency will be extracted.
        :type html_page: str
        """
        self.logger = logger
        self.config = config
        self.html_page = html_page
        self.price: Optional[float] = None
        self.currency: Optional[str] = None

        self._initialize_parser()

    def _initialize_parser(self) -> None:
        """
        Initializes the parser by extracting price and currency from the provided HTML content.

        This method sets the `price` and `currency` attributes based on the extracted data.
        If the extraction fails, these attributes will remain None.
        """
        try:
            self.price, self.currency = self._extract_price_currency()
        except Exception as e:
            self.logger.error(f"Failed to initialize GeminiParser: {str(e)}")
            self.price, self.currency = None, None

    def _get_gemini_api_key(self) -> str:
        """
        Retrieves the Gemini API key from the configuration.

        :return: The Gemini API key.
        :rtype: str
        :raises ValueError: If the API key is missing in the configuration.
        """
        gemini_api_key = self.config.get('Crawler', 'gemini_api_key')
        if not gemini_api_key:
            raise ValueError("Gemini API key is missing in the configuration.")
        return gemini_api_key

    def _prepare_prompt(self) -> str:
        """
        Prepares the prompt for the Gemini API.

        :return: The prompt string to be sent to the Gemini API.
        :rtype: str
        """
        return (
            "Extract the price and currency in ISO Format from the following HTML content. "
            "Respond in this exact JSON format: {\"Price\": float, \"Currency\": str}. "
            f"HTML Content: {self.html_page}"
        )

    def _send_request(self, prompt: str) -> str:
        """
        Sends a request to the Gemini API and returns the raw response.

        :param prompt: The prompt string to be sent to the API.
        :type prompt: str
        :return: The raw response text from the Gemini API.
        :rtype: str
        :raises RuntimeError: If no output is received from the API.
        """
        api_key = self._get_gemini_api_key()
        genai.configure(api_key=api_key)
        model = genai.GenerativeModel("gemini-1.5-flash")
        response = model.generate_content(prompt)
        if not response or not hasattr(response, 'text'):
            raise RuntimeError("No output received from the Gemini API.")
        return response.text.strip()

    def _parse_response(self, raw_text: str) -> Tuple[Optional[float], Optional[str]]:
        """
        Parses the JSON response and extracts price and currency.

        :param raw_text: The raw response text from the Gemini API.
        :type raw_text: str
        :return: A tuple containing the price and currency.
        :rtype: Tuple[Optional[float], Optional[str]]
        :raises ValueError: If the response format is invalid or JSON decoding fails.
        """
        # Clean up the Markdown code fencing
        if raw_text.startswith("```json") and raw_text.endswith("```"):
            raw_text = raw_text[7:-3].strip()

        self.logger.debug(f"Raw Gemini API response: {raw_text}")

        try:
            parsed_response = json.loads(raw_text)
            price = parsed_response.get("Price")
            currency = parsed_response.get("Currency")
            if isinstance(price, (float, int)) and isinstance(currency, str):
                return float(price), currency
            else:
                raise ValueError("Invalid response format.")
        except json.JSONDecodeError:
            raise ValueError(f"Failed to decode JSON: {raw_text}")

    def _extract_price_currency(self) -> Tuple[Optional[float], Optional[str]]:
        """
        Extracts the price and currency from the HTML content using the Gemini API.

        :return: A tuple containing the extracted price and currency, or (None, None) if extraction fails.
        :rtype: Tuple[Optional[float], Optional[str]]
        """
        try:
            prompt = self._prepare_prompt()
            raw_text = self._send_request(prompt)
            return self._parse_response(raw_text)
        except Exception as e:
            self.logger.error(f"Error in extracting price and currency: {str(e)}")
            return None, None

    def get_price(self) -> Optional[float]:
        return self.price

    def get_currency(self) -> Optional[str]:
        return self.currency
