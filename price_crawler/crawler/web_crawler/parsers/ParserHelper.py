from typing import Optional
import logging


class ParserHelper:

    @staticmethod
    def parser_price_to_float(logger: logging, price: str) -> Optional[float]:
        """
        Parses a price string and converts it to a float.

        This method handles different formats of price strings, including
        those with commas and periods as thousand or decimal separators. It
        returns the corresponding float value if successful. If the price
        string cannot be parsed, it logs an error and returns None.

        :param logger: The logger object to log error messages.
        :type logger: logging
        :param price: The price string to be parsed, which can include
                      commas and periods in different formats.
        :type price: str
        :return: The parsed price as a float if successful, or None if the
                 price string could not be parsed.
        :rtype: Optional[float]
        """
        # no commas in price -> directly convert
        if price.find(".") == -1:
            if price.find(",") == -1:
                return float(price)

        # comma symbol: ,
        if price[-3] == ",":
            # remove all . in string
            thousand_sep_removed = price.replace('.', '')
            ret = thousand_sep_removed.replace(',', '.')
            return float(ret)

        # comma symbol: ,
        if price[-3] == ".":
            # remove all . in string
            thousand_sep_removed = price.replace(',', '')
            return float(thousand_sep_removed)

        # no comma but thousand seperator in price
        # e.g. 1.575
        if price.find(".") != -1:
            ret = price.replace('.', '')
            return float(ret)

        # e.g. 1,578
        if price.find(",") != -1:
            ret = price.replace(',', '')
            return float(ret)

        logger.error("Could not parse price to float. Price: " + str(price))
        return None