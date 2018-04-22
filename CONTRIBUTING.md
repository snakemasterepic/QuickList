# Overview

Contribution is welcome as long as you follow the contribution guidelines listed below.

# Readability
Your code should be easy to read and trace.  Comment as much as necessary to guide the reader through what the code is doing.  All methods, fields, classes, and interfaces (including those that are private) must be documented with Javadoc style comments.  Give variables useful names unless the variable's task is easy enough to determine from a single glance at the code (such as an int i that is just used to access elements in an array).

# Changes
Ensure that all of the tests work when you upload changes.  Do not make changes that just "clean up formatting" (NetBeans does that with ALT+F).  Changes to the code should respect the following values listed from most important to least important:
- Functionality: The code functions correctly and does not crash unless it is supposed to do so
- Performance: The code runs relatively quickly
- Maintainability: The code is easy to maintain, and changes can be quickly and easily made
- Readability: The code is easy to read and understand.

# CI
This project uses continuous integration managed by [Semaphore](https://semaphoreci.com/snakemasterepic/quicklist-2).  This involves building and testing after each push.
