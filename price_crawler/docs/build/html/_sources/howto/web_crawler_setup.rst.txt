HOWTO: Setup the Web Crawler
============================

To run the web crawler with Python, you first need to install the necessary prerequisites. Once the dependencies are set up, configure the crawler according to your needs. After the configuration, you can run the crawler to start collecting data.

----------------
1. Prerequisites
----------------

.. _setup-preq_label:

^^^^^^^^^^^^^
Install pyenv
^^^^^^^^^^^^^

| Install `pyenv` for your Platform: `Installation Instructions <https://github.com/pyenv/pyenv?tab=readme-ov-file#installation>`_
| Setup your shell environment: `Setup <https://github.com/pyenv/pyenv?tab=readme-ov-file#b-set-up-your-shell-environment-for-pyenv>`_
| Install python build dependencies: `Setup <https://github.com/pyenv/pyenv/wiki#suggested-build-environment>`_

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Install python version 3.12.7
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. code-block:: bash

   # install specific python version
    pyenv install 3.12.7
    # create a virtual env
    pyenv virtualenv 3.12.7 ase24_venv

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Activate the virtual environment
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. code-block:: bash

    # activate the env
    pyenv activate ase24_venv
    # deactivate the env: source deactivate

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Install all web crawler dependencies
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. code-block:: bash

    pip install -r 24ws-ase-pr-qse-02/price_crawler/requirements.txt


----------------
2. Configuration
----------------

| Configure the settings of the web crawler to fit your needs.
| All possible settings are explained in the :ref:`Configuration Page <config_label>`.
| The configuration file can be found :ref:`here <config-file_label>`.


----------------------
3. Run the web crawler
----------------------

.. code-block:: bash

    cd 24ws-ase-pr-qse-02/price_crawler
    python3 main.py
