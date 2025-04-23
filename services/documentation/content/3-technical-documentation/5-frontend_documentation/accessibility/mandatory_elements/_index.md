---
title: "Mandatory elements"
description: "Discover all mandatory elements in a Web page"
date: 2023-12-28T08:20:38+01:00
weight: 20
---

In this section, you will learn what are the mandatory elements on a Web page to respect accessibility

#### Definition

Mandatory elements in a Web page are various. They include a valid source code, well-named page titles,
HTML tags used for their proper meaning and also multilingual texts.

#### A valid source code

Assistive technologies are based on DOM (Document Object Model) for interaction with the user. If the HTML
code contains code errors, this may render some systems inoperative. Mainly impacted users are blind,
visually and motor impaired ones.

It is therefore crucial to add a Doctype declaration like ```<!DOCTYPE html>```.
It makes sure the document will be parsed the same way by different browsers.

A source code is also valid when tags, attributes and attribute values respect the writing rules and when
the opening and closing of the tags are conformed.

#### Page titles

Page titles are the first information available to users. In their absence or irrelevance, blind and severely
visually impaired users who use screen readers (page title is the first thing they read) will have great difficulty
finding a page in the list of open tabs.
Because the page title is read on each page, it should be short (generally a few words) and describe the page
content. The page title may contain the site name, and there is no rule about whether this information should come
before or after the descriptive information (e.g., "Site name: description" or "Description: site name" both works).

#### Use HTML tags for their proper meaning

Blind and visually impaired people use screen readers that rely on the semantics of tags.
If the use of tags is misused, the rendering may become incomprehensible.
For example, the use of ```div``` or ```span``` elements to create paragraphs is considered non-compliant.

#### Multilingual texts

Screen readers make a sound restitution of words, so it is important that the vocal synthesis pronunciation
in the language defines the words to be understood.
The main language of a web page is defining on the ```<html>``` tag but when a word is in a different language
from the main one, it is necessary to specify it with the attribute ```lang```.
However, there are exceptions like proper names or common name for a foreign language.
