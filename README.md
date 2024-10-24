# ~ Calendar Assignment in Java - Finished ~


The app processes iCalendar files (iCal4j) and provides a graphical user interface (Swing) to create, read, and write calendar data, converting command-line operations (First part of the Assignment) into user-friendly buttons and controls (Second part of the Assignment). Started as a group assignment but besides a few UI lines and a bunch of Unnecessary delays by my Groupmates i did 80%+ of the work to Finish this assignment.

## Acquired Skills:
 - Using Maven, a Build automation tool! (quite fun)
 - Creating a UI in Java (scary i know)

## How to use

Make sure you are in the correct path (where `pom.xml` is located).

Run `mvn clean install` then Run `mvn package` and check the `target` folder.

Execution: `java -jar miCalendari-2.0.jar`


## Classes

- **UIApp:** Manages all the UI functionalities.
- **CalendarManager:** Created to handle most of the functions/methods related to iCal4j.
- **Event:** Parent class that manages all Events.
- **Appointment:** Extends the Event class.
- **Task:** Extends the Event class.
- **CalendarApp (Main):** Created to handle user needs, as it contains the main method and all the views/settings the user needs for the smooth operation of the program. (CalendarApp was used in the First Part, while UIApp replaces it in the Second Part of the assignment.)



## From Concept to Completion (UI)
<b>
<div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/5cf26298-e25e-49d1-a5e9-a611bd964097" alt="image1" width="600"/>
    <p>Image 1: Turning my Imagination into a Photoshop Design. </p>
</div>
</b>

## 

<div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/e43b0287-3769-453b-b5bb-f99218089ab7" alt="image2" width="600"/>
    <p>Image 2: Photoshop re-Design with the Half completed UI + More imagination.</p>
</div>
</b>

## 

<div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/db273a41-cfb5-476e-9317-82f3b28d6ff9" alt="image3" width="600"/>
    <p>Image 3: Perfection! Haven't been more proud about my art. </p>
</div>
</b>
</b>

## 


## License
MIT License

Copyright (c) 2023-2024 EvanLei-git

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
