## Content Aware Image Resizer

### Table of Contents

1. [Motivation](#Motivation)

2. [Features](#Features)

3. [Screenshots](#Screenshots)

4. [Background](#Background)

5. [How it works](#How-it-works)

6. [Optimizations](#Optimizations)

7. [Credits](#Credit)

8. [Installation/Contribution](#Can-I-contribute-or-try-it-out?)

### Motivation

Like a few of my other projects published on GitHub, I started working on this project while learning about data structures and algorithms <sup>[[1]](#1)</sup>. Part 2 of the  course referenced, dives into graph theory with topics such as spanning trees, tries, max/min flow applications and a lot of other good stuff (I highly recommend checking them out!).   

This is definitely one of my favorite applications that uses the knowledge learned from those courses in graph theory. It's both pratical, intuitive and very interesting. There were also many places to learn and try out different things. For example I ended up implementing a few different graph traversing algorithims such as Dijkstra's and Bellman Ford's. I found that in the implementations used, Bellman Ford's provided the most optimal performance in this application.

### The Good Stuff

I'll briefly introduce the functionality behind this project, including the backend such as the algorithims used, how everything works and a few of the optimizations.

#### Features

- Graphical User Interface

- Statistics 
   - _Current image size_ relative to the original image

- Import & Export  (PNG, JPG)

- Resize Images in Real Time using a Slider

- Partial Resizing while the Image is being Processed

#### Screenshots
![Demo of Image Resizing](prewiew.gif)

#### Credit
I wouldn't have been able to write this project without the resources from the algorithms courses<sup>[[1]](#1)</sup> by Princeton . 

I also used their [Picture Class](https://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/Picture.html) which allowed for the internal storing of pictures to allow processing. 

Therefore shout out to the folks at Princeton: _Robert Sedgewick_, _Kevin Wayne_ and all the other people who contributed to these exceptionally high quality resources.

<a id="1">[1]</a>: I'd highly recommend checking out [Princeton's Algorithms](https://www.coursera.org/learn/algorithms-part2) courses in combination with their textbook, they are very well versed! 
