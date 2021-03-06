# ServerShop3D



![](https://github.com/fahadzafar/AppShop3D/blob/master/app/src/main/res/drawable/logo.png)

#Goal of the App:
Allows users to customize 3D models, then place an order for a 3D print and pay through the app. This Server does the heavy lifting to create the customized model based on incoming user preferences from the app and place the order.

#Description:
We always want to put our own personal touch on things, so why cant we do it with collectible, miniatures and figures ? Well now you can. Select a collectible that you like, and color is the way you want. Enhance their features or make them look weird. That's the fun part. Then simply order it and we will ship it to your doorstep to treasure it forever and to share it with friends and family. Make an entire fleet of spaceships or get your own orc that is "not green", or even a "zombie" with an orange skin. You can do all that all that with Shop3D, a simple and easy way to cast your creativity on collectibles.



The design doc for Shop3D and its server can be seen [here](https://drive.google.com/open?id=0BzkvMWM-w80JZFg1V2V0bnh1ZzQ).

The server works using a passive approach, saves on a lot of money and dont need to use AWS 24/7. The server can run on any laptop, desktop and AWS (even all at the same time) and is multi-threaded with full asynchronous compute. This server also auto adjusts pool timing when checking the work request queue and can alter pooling time based on usage. The core of this order placement can be seen below which goes through about 15 steps to place an order with a customized 3D model per order,
https://github.com/fahadzafar/ServerShop3D/blob/master/src/org/shop3d/server/main/WorkerThread_FulfillOrder.java




For the Parse table that stores the models see [here](https://drive.google.com/open?id=0BzkvMWM-w80JSDMxYVZ3ZmZ0QTA) and [here](https://drive.google.com/open?id=0BzkvMWM-w80JSnRsbVhEZG1la2c)

For all model categories see [here](https://drive.google.com/open?id=0BzkvMWM-w80JUm1wQTVfRGd3MGc)

For more images about the app go [here](https://drive.google.com/open?id=0BzkvMWM-w80JQUNESFl2dVM1djg).


The app for this applicataion is [here](https://github.com/fahadzafar/AppShop3D)

App usage Video [here](https://www.youtube.com/watch?v=9ab7zeuOSMs).


Note: Parse will be discontinued after January 2017, this app+server will not be migrated.

