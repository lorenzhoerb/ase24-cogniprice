# Frontend

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 18.2.10.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.


---

# Testing the Application with Selenium IDE

This documentation explains how to set up and execute tests for the application using **Selenium IDE**.

## **1. What is Selenium IDE?**
Selenium IDE is a browser extension for recording, editing, and playing back user interactions with a web application. It allows testers to automate functional tests for web applications without needing advanced programming skills.

---

## **2. Prerequisites**
Before running the Selenium IDE tests, ensure the following:
- You have **Google Chrome** or **Mozilla Firefox** installed.
- Selenium IDE is installed as a browser extension.
- The application is running and accessible at the correct URL (e.g., `http://localhost:4200` for a local environment).

---

## **3. Setting Up Selenium IDE**

1. **Install Selenium IDE**:
  - **For Chrome**: [Download from Chrome Web Store](https://chrome.google.com/webstore/detail/selenium-ide/mooikfkahbdckldjjndioackbalphokd).
  - **For Firefox**: [Download from Firefox Add-ons](https://addons.mozilla.org/en-US/firefox/addon/selenium-ide/).

2. **Import Test Suite**:
  - Open Selenium IDE.
  - Click on the **Project** menu (three dots in the top-left corner).
  - Select **Open Project** and upload the provided `.side` file (e.g., `CogniPrice.side`).

3. **Verify Test Configuration**:
  - Ensure the base URL in Selenium IDE matches the environment URL:
    - Click **Settings** in Selenium IDE.
    - Set the **Base URL** to your application URL (e.g., `http://localhost:4200`).

---

## **4. Running the Tests**

1. **Open Selenium IDE**:
  - Launch the Selenium IDE extension from your browser.
  - Load the `.side` file if it’s not already loaded.

2. **Run a Test Suite**:
  - Click the **Run All Tests in Suite** button (green play button).
  - Select the desired test suite (e.g., `Authentication Suite`).

3. **Run a Single Test**:
  - Click on a specific test case in the left-hand list.
  - Click the **Run Current Test** button.

---

## **5. Viewing Test Results**
After running the tests:
- **Execution Log**:
  - The bottom panel in Selenium IDE shows a detailed log of test execution, including passed and failed steps.
- **Result Summary**:
  - A green checkmark indicates a passed test.
  - A red cross indicates a failed test.

---

## **6. Modifying and Adding Tests**

1. **Record New Tests**:
  - Click the **Record a New Test in Project** button.
  - Interact with the application in your browser to record actions.
  - Stop recording and save the test.

2. **Edit Existing Tests**:
  - Select the test case you want to edit.
  - Modify commands or add new steps directly in the Selenium IDE interface.

3. **Use Assertions**:
  - Add **assertion commands** to verify application behavior (e.g., `assertText`, `assertElementPresent`).
  - Example: Verify a success message after registration:
    ```plaintext
    Command: assertText
    Target: css=div.success-message
    Value: Registration successful!
    ```

4. **Test Data**:
  - Use variables in Selenium IDE to dynamically insert test data:
    - Example: Use a variable for email:
      ```plaintext
      Command: type
      Target: id=email
      Value: ${testEmail}
      ```
