from abc import ABC

class InvalidLoggerObjectException(ABC, Exception):
    """
    Custom exception raised when a URL could not be parsed.
    """
    def __init__(self, message: str) -> None:
            self.message = message
            self.error_name = "INVALID_LOGGER_OBJECT"
            super().__init__(self.message, self.error_name)

class InvalidConfigObjectException(ABC, Exception):
    """
    Custom exception raised when a URL could not be parsed.
    """
    def __init__(self, message: str) -> None:
            self.message = message
            self.error_name = "INVALID_CONFIG_OBJECT"
            super().__init__(self.message, self.error_name)