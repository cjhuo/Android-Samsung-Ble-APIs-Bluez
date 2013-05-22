Android-Samsung-Ble-APIs-Bluez
==============================

BLE APIs Samsung provided from Jellybean based on Bluez stack. Compatible with Galaxy series mobile device with Bluetooth 4.0 chip. Not working from Android 4.2.2 since Samsung switched to use stack from broadcom


Background
==========

Recently Samsung announced [BLE SDK](http://developer.samsung.com/ble) for supporting their devices to communicate 
with 'Smart' device using bluetooth low energy protocol. However, the support is only limited to their mobile devices
running with Android 4.2.x (Level 17). To our best knowledge, Galaxy S4 comes with 4.2 which means the current main trend devices including Galaxy S3, Note 2 (currently latest official update is Jellybean 4.1.x - Level 16) won't be able to get the support at this point. It really depends on when Samsung will push out update for these 'old' devices to make them work with latest SDK.

With an assigned task, I started to exploit BLE support on Android. Before this, I had no knowledge at all on Android
development. I did some iOS development with the BLE stack provided by Apple though. Unlike the case with Apple, I found out
that there is no standard support for BLE in Android world. Instead, vendors provides their own support for their own
devices. Motorola has their [API](http://www.motorola.com/sites/motodev/library/bluetooth_apis.html) but I don't know if
it still works with the latest version of Android OS since I don't have the device to test. HTC includes BLE support in 
their [OpenSense SDK](http://www.htcdev.com/devcenter/opensense-sdk) which seems get most support among others since
[Bluetooth.org](http://bluetooth.org) has chosen it as the Smart-Ready device for Android development in their [BLE development kit](http://developer.bluetooth.org/DevelopmentResources/Pages/Quick-Start-Kit.aspx).
But here we focus on the test devices I got.

Difference on S3 and S4
=======================
What I got for test developement are both coming from Samsung who claims has the most market share in Android world, 
the Galaxy S3 (IGH-747) and Galaxy S4 (I9500). I got the S3 with contract upgrade from AT&T last year (Sorry for leaving
Apple, but S3 was way cheaper than iPhone 5 at that time and I wanted to save the cost) and got the S4 recently for the test 
developement. Altough APIs on both the platforms are provided by Samsung they are using different stack underneath (mentioned in their BLE SDK API Guide). 

S3's API was developed based on stack from [Bluez](http://www.bluez.org/). I guess HTC's APIs is also based on Bluez but not sure.

S4's API is developed based on stack from broadcom called Bluedroid. I guess Samsung the reason why Samsung made this change
is because Samsung's mobile devices reply on Broadcom's bluetooth chip and Broadcom claims to 
[provide their bluetooth stack in Android 4.2 for free](http://www.broadcom.com/press/release.php?id=s721534).


Reason why I dig out the APIs on S3
===================================
Of course first reason is because I want to have the little board built from our lab with TI's CC2540 BLE chip talk to not only S4 but also S3 that I have. Indicated from the SDK API Guide Samsung provided on their SDK website, they provided the API that works with Bluez stack in the past which they called verion 1.0. And now the APIs on S4 is 2.0. However, I couldn't find the place whey they used to release their API with version 1.0. In fact, I saw developers kept asking for it from the time Galaxy S3 was introduced which claims Smart-Ready.

Clues
=====
Fortunately, I found out some apps on play store that make use of the BLE API from Samsung such [Bluetooth Smart Scanner](https://play.google.com/store/apps/details?id=com.semilink.smartscanner&feature=search_result#?t=W251bGwsMSwyLDEsImNvbS5zZW1pbGluay5zbWFydHNjYW5uZXIiXQ..) and [Pebble](https://play.google.com/store/apps/details?id=com.semilink.pebble.pxpmain&feature=search_result#?t=W251bGwsMSwyLDEsImNvbS5zZW1pbGluay5wZWJibGUucHhwbWFpbiJd) from Semilink, and [nRF utility](https://play.google.com/store/apps/details?id=com.nordic.nordicbleapp&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5ub3JkaWMubm9yZGljYmxlYXBwIl0.) from nordic. It shows me that the API must have been published, but probably not publicly. I don't know whether those company who got the API has contract with Samsung or something. But I decided to get the API out and give it a try.

Getting the API out
===================
All I want to do is the jar file of Samsung's API which they claims to have provided before. To respect others work I only share the compiled API jar here and make a test app by my own.


How to use the API to make a app
================================

Knowledgement
=============
Thanks to the great tools [dex2jar](https://code.google.com/p/dex2jar/), I have managed to get the API out
