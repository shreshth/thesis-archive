# Thesis archive

Archive of all my code for thesis. Abstract pasted below:

Smartphones are becoming more and more universal. An increasing number of services on these smartphones allow people to access the internet, through wireless networks and cellular data connections such as 3G. The explosion of 3G usage is causing a lot of pressure on cellular data resources, driving up costs and causing congestions. We develop a system named DroiDTN, with the goal of smartly scheduling network traffic on smartphones between wifi and 3G, so as to reduce cost of 3G, while also maintaining good quality-of-service. The key notion is that of delay tolerance. Some network traffic is tolerant to delays - for example, email syncing that occurs in the background can be delayed since the user isn't directly interacting with the process. We can leverage this delay tolerance to hold off sending network traffic over 3G, and instead wait for wifi if it is expected soon. We developed DroiDTN as a system that learns about the past characteristics of the user and the world, and uses this to solve the network scheduling problem. A part of this is the delay tolerance estimator, which tracks the network usage statistics of all apps, and analyzes these to predict each app's delay tolerance. There is another component that tracks the wifi and 3G characteristics at di erent locations as the user walks around, and uses these to predict the expected time it will take for the user to get in range of usable wifi. Then, if an app tries to make a network request, we can gauge its delay tolerance, and if we see that this delay tolerance value is more than the time it will take to reach wifi, we can choose to delay the network traffic in the hopes of sending it over wifi later. This reduces cost and energy consumption. Our analysis of our system shows promising results. The individual components of the system were tested independently - for example, the delay tolerance estimator performs remarkably well in estimating delay tolerance values. Overall, DroiDTN is a usable system that can run on an Android smartphone without any developer or user input required. Further, some preliminary experiments have shown its efficacy in reducing the number of requests made over 3G.


