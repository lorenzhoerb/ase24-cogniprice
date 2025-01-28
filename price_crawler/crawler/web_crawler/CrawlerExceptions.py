from abc import ABC

class URLParseException(ABC, Exception):
    """
    Custom exception raised when a URL could not be parsed.
    """
    def __init__(self, message: str) -> None:
            self.message = message
            self.error_name = "URL_COULD_NOT_PARSE"
            super().__init__(self.message, self.error_name)

class RobotParseException(ABC, Exception):
    """
    Custom exception raised when there is an error parsing a robots.txt file.
    """
    def __init__(self, message: str) -> None:
            self.message = message
            self.error_name = "ROBOT_PARSE_ERROR"
            super().__init__(self.message, self.error_name)