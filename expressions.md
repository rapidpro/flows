---
layout: spec
title: Expressions
permalink: /expressions/
---
# Expressions, Version 8

RapidPro provides a template language that allows for the substitution of variables into a string.

```
Hello @contact.name, how old are you?
```

In RapidPro, expressions are also used when assigning to fields and within operands in RuleSets to determine what is
being evaluated against. In all cases expressions are evaluated against the current context.

# Variables

The context defines the variables available for substitution in expressions.

### Contact Variables
The ```@contact``` variables all refer to the contact that will receive a message. Note that this will be the contact
participating in a flow when sending a response to them, but will be the respondent instead when sending an message
to somebody else (say in a Send action)

 * ```@contact``` - The name of the contact if one is set, otherwise their number. ex: "Ben Haggerty"
 * ```@contact.name``` - The name of the contact if one is set, otherwise their number. ex "Ben Haggerty"
 * ```first_name``` - The first name of the contact if one is set, otherwise their number. ex: "Ben"
 * ```@contact.tel``` - The telephone number for the contact, in a human readable format. ex: "(206) 555 1212"
 * ```@contact.tel_e164``` - The telephone number for the contact, in E164 format. ex: "+12065551212"

Contact will also have any Contact Fields you have saved to them available. The name of the variable is the same as the Contact Field name, except all lowercase and with spaces replaced with _.  For example, if you added a new Contact Field named "Email Address", then you could refer to it as @contact.email_address.

### Flow Variables
The ```@flow``` variables refer to values collected within the current flow. This lets you refer to values at later parts in the flow or update Contact Fields with a value entered by the Contact.

Just as the variables on Contact Fields, the name of the variable will be the same as the Rule name in the flow, except it will be lowercase with all spaces replaced with _. For example a Rule in a flow named "Delivery Date" can be referenced as @flow.delivery_date

Below ```[variable-name]``` refers to that Rule name in your flow.

 * ```@flow``` - All flow variables collected to this point. ex: "Name: Foo, Age: 32"
 * ```@flow.[variable-name]``` - Which value collected by you rule. ex: "32"
 * ```@flow.[variable-name].category``` - Which category matched on your rule. ex: "Valid Age"
 * ```@flow.[variable-name].text``` - The full text that was run against your rule. ex: "My age is 32"
 * ```@flow.[variable-name].value``` - The full text that was run against your rule. ex: "My age is 32"
 * ```@flow.[variable-name].time``` - The date and time when this flow value was collected. ex: "2014-05-02 19:11:50"

### Step Variables

The ```@step``` variables refer to values related to the message currently being processed. That is, if a step in a flow was triggered by an incoming message, then the @step variables are all related to that incoming message.

 * ```@step``` - The value of the step, usually the content of the text message. ex: "help"
 * ```@step.value``` - The value of the step, usually the content of the text message. ex: "help"
 * ```@step.date``` - The date and time when the step occurred.
 * ```@step.contact``` - The contact (and all associated variables) that is executing this Flow

### Channel Variables

The ```@channel``` variables refer to which phone number or device a message was received with. For example, if you connect an Android phone to your account, the ```@channel``` variables will all refer to that Android phone.

 * ```@channel``` - the phone number that this message came in on, in a human readable format. ex: "(206) 555 1212"
 * ```@channel.name``` - the name of the channel (this can be edited on it's page) ex: "Nexus 5"
 * ```@channel.tel``` - the human readable telephone number for the channel ex: "(206) 555 1212"
 * ```@channel.tel_e164``` - the E164 formatted telephone number. ex: "+12065551212"

### Date Variables

The ```@date``` variables all refer to the time when this flow step will be executed. These can be useful when you want to update a Contact Field to the current time, or include the time in a message sent to someone else.

 * ```@date``` - The current date and time. ex: "02-05-2014 20:08"
 * ```@date.today``` - The current date. ex: "02-05-2014"
 * ```@date.yesterday``` - The date yesterday. ex: "01-05-2014"
 * ```@date.tomorrow``` - The date tomorrow. ex: "03-05-2015"

# Expressions Functions

The syntax used by RapidPro expressions is modeled on that used for formulas in Microsoft Excel™.

Just as variables are substituted when starting with ```@``` expressions are indicated using the syntax
```@(expression)```, for example ```Hello @(UPPER(contact.first_name))```

Function names are not case-sensitive so UPPER is equivalent to upper. RapidPro supports a subset of the functions
available in Excel and these are listed below.

Expressions can also include arithmetic with the add (+), subtract (-), multiply (*), divide (/) and exponent (^) operators, e.g.
```Result is @(1 + (2 - 3) * 4 / 5 ^ 6) units```
Notice that the expression is enclosed in parentheses to tell RapidPro where it ends.

You can also join strings with the concatenate operator (&), e.g.
```Hello @(contact.first_name & " " & contact.last_name)```

## Logical comparisons

A logical comparison is an expression which evaluates to TRUE or FALSE﻿. These may use the equals (=), not-equals (<>),
greater-than (>), greater-than-or-equal (>=), less-then (<) and less-than-or-equal (<=) operators, e.g.
```@(contact.age > 18)```

Note that when comparing text values, the equals (=) and not-equals (<>)﻿ operators are case-insensitive.

## Date and time arithmetic

RapidPro emulates most of the date and time operations available in Excel. For example, to add 7 days to a date, just add the number 7, e.g.

```See you next week on the @(date.today + 7)```

To rewind a date by a certain number of days, just subtract that number, e.g.

```Yesterday was @(date.today - 1)```

To add or subtract months, use the EDATE function, e.g.

```Next month's meeting will be on @(EDATE(date.today, 1))```

To change the time of datetime value, add or subtract the result of the TIME function, e.g.

```2 hours and 30 minutes from now is @(date.now + TIME(2, 30, 0))```

## Function Reference

Function arguments in square brackets ([ ... ]) are optional.

### Date and time functions

#### DATE(year, month, day﻿)
Defines a new date value

```This is a date @DATE(2012, 12, 25)```

#### DATEVALUE(text)
Converts date stored in text to an actual date, using your organization's date format setting

```You joined on @DATEVALUE(contact.joined_date)```

#### DAY(date)
Returns only the day of the month of a date (1 to 31)

```The current day is﻿@DAY(contact.joined_date)```

#### EDATE(date, months)
Moves a date by the given number of months

```Next month's meeting will be on @EDATE(date.today, 1)```

#### HOUR(datetime)
Returns only the hour of a datetime (0 to 23)

```The current hour is @HOUR(NOW())```

#### MINUTE(datetime)
Returns only the minute of a datetime (0 to 59)

```The current minute is @MINUTE(NOW())```

#### MONTH(date)
Returns only the month of a date (1 to 12)

```The current month is @MONTH(NOW())```

#### NOW()
Returns the current date and time

```It is currently @NOW()```

#### SECOND(datetime)
Returns only the second of a datetime (0 to 59)

```The current second is @SECOND(NOW())```

#### TIME(hours, minutes, seconds)
Defines a time value which can be used for time arithmetic

```2 hours and 30 minutes from now is @(date.now + TIME(2, 30, 0))```

#### TIMEVALUE(text)
Converts time stored in text to an actual time

```Your appointment is at @(date.today + TIME("2:30"))```

#### TODAY()
Returns the current date

```Today's date is @TODAY()```

#### WEEKDAY(date)
Returns the day of the week of a date (1 for Sunday to 7 for Saturday)

```Today is day no. @WEEKDAY(TODAY()) in the week```

#### YEAR(date)
Returns only the year of a date

```The current year is =YEAR(NOW())```

### Logical functions

#### AND(arg1, arg2, ...)
Returns TRUE if and only if all its arguments evaluate to TRUE

```@AND(contact.gender = "F", contact.age >= 18)```

#### IF(arg1, arg2, ...)
Returns one value if the condition evaluates to TRUE, and another value if it evaluates to FALSE

```Dear @IF(contact.gender = "M", "Sir", "Madam")```

####OR(arg1, arg2, ...)
Returns TRUE if any argument is TRUE

```@OR(contact.state = "GA", contact.state = "WA", contact.state = "IN")```

### Math functions

#### ABS(number)
Returns the absolute value of a number

```The absolute value of -1 is @ABS(-1)```

#### MAX(arg1, arg2, ...)
Returns the maximum value of all arguments

```Please complete at most @MAX(flow.questions, 10) questions```

#### MIN(arg1, arg2, ...﻿)
Returns the minimum value of all arguments

```Please complete at least @MIN(flow.questions, 10) questions```

#### POWER(number, power)
Returns the result of a number raised to a power - equivalent to the ^ operator

```2 to the power of 3 is @POWER(2, 3)```

#### SUM(arg1, arg2, ...)
Returns the sum of all arguments, equivalent to the + operator

```You have =SUM(contact.reports, contact.forms) reports and forms```

### Text functions

#### CHAR(number)
Returns the character specified by a number

```As easy as @CHAR(65), @CHAR(66)﻿, @CHAR(67)```

#### CLEAN(text)
Removes all non-printable characters from a text string

```You entered @CLEAN(step.value)```

#### CODE(text)
Returns a numeric code for the first character in a text string

```The numeric code of A is @CODE("A")```

#### CONCATENATE(args)
Joins text strings into one text string

```Your name is @CONCATENATE(contact.first_name, " ", contact.last_name)```

#### FIXED(number, [decimals], [no_commas])
Formats the given number in decimal format using a period and commas

```You have @FIXED(contact.balance, 2) in your account```

#### LEFT(text, num_chars)
Returns the first characters in a text string

```You entered PIN @LEFT(step.value, 4)```

#### LEN(text)
Returns the number of characters in a text string

```You entered @LEN(step.value) characters```

#### LOWER(text)
Converts a text string to lowercase

```Welcome @LOWER(contact)```

#### PROPER(text)
Capitalizes the first letter of every word in a text string

```Your name is @PROPER(contact)```

#### REPT(text, number_times)
Repeats text a given number of times

```Stars! @REPT("*", 10)```

#### RIGHT(text, num_chars)
Returns the last characters in a text string

```Your input ended with ...=RIGHT(step.value, 3)```

#### SUBSTITUTE(text, old_text, new_text, [instance_num])
Substitutes new_text for old_text in a text string. If instance_num﻿ is given, then only that instance will be substituted

```@SUBSTITUTE(step.value, "can't", "can")```

#### UNICHAR(number)
Returns the unicode character specified by a number

```As easy as =UNICHAR(65), =UNICHAR(66)﻿, =UNICHAR(67)```

#### UNICODE(text)
Returns a numeric code for the first character in a text string

```The numeric code of A is @UNICODE("A")```

#### UPPER(text)
Converts a text string to uppercase

```WELCOME =UPPER(contact)!!```

### RapidPro Specific Functions
These functions are not found in Excel but have been provided for the sake of convenience.

#### FIRST\_WORD(text)
Returns the first word in the given text - equivalent to WORD(text, 1)

```The first word you entered was @FIRST_WORD(step.value)```

#### PERCENT(number)
Formats a number as a percentage

```You've completed @PERCENT(contact.reports_done / 10) reports```

#### READ\_DIGITS(text)
Formats digits in text for reading in TTS

```Your number is @READ_DIGITS(contact.tel_e164)```

#### REMOVE\_FIRST\_WORD(text)
Removes the first word from the given text. The remaining text will be unchanged
```You entered @REMOVE_FIRST_WORD(step.value)```

#### WORD(text, number, [by_spaces])
Extracts the nth word from the given text string. If stop is a negative number, then it is treated as count backwards from the end of the text. ﻿If by_spaces is specified and is TRUE then the function splits the text into words only by spaces. Otherwise the text is split by punctuation characters as well

```@WORD("hello cow-boy", 2)``` will return "cow"

```@WORD("hello cow-boy", 2, TRUE)``` will return "cow-boy"

```@WORD("hello cow-boy", -1)``` will return "boy"

#### WORD\_COUNT(text, [by_spaces]﻿)
Returns the number of words in the given text string. If by_spaces is specified and is TRUE then the function splits the text into words only by spaces. Otherwise the text is split by punctuation characters as well

```You entered @WORD_COUNT(step.value) words```

#### WORD\_SLICE(text, start, [stop], [by_spaces]﻿)
﻿Extracts a substring of the words beginning at start, and up to but not-including stop. If stop is omitted then the substring will be all words from start until the end of the text. If stop is a negative number, then it is treated as count backwards from the end of the text. If by_spaces is specified and is TRUE then the function splits the text into words only by spaces. Otherwise the text is split by punctuation characters as well

```@WORD_SLICE("TextItexpressions are fun", 2, 4)``` will return 2nd and 3rd words "expressions are"

```@WORD_SLICE("TextItexpressions are fun", 2)``` will return "expressions are fun"

```@WORD_SLICE("TextItexpressions are fun", 1, -2)``` will return "TextItexpressions"

```@WORD_SLICE("TextItexpressions are fun", -1)``` will return "fun"
