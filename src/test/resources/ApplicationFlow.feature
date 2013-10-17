Feature: Application Flow
  As a user
  I want a predictable and simple UI
  so that I can use the application.

Scenario: Launch
  Given a command line terminal
  When I run the application
  Then I will be greeted with the message "Welcome!\nPlease select a menu option:"
  And a list of menu options.