from abc import ABC, abstractmethod
from typing import Optional

class IParser(ABC):

    @abstractmethod
    def get_price(self) -> Optional[float]:
        """
        Retrieves the price associated with the current object.
        """
        pass

    @abstractmethod
    def get_currency(self):
        """
        Retrieves the currency associated with the current object.
        """
        pass