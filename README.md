# IOT_Adafruit_Application
- Adafruit IO is a platform designed (by us!) to display, respond, and interact with your project's data. We also keep your data private (data feeds are private by default) and secure (we will never sell or give this data away to another company) for you. It's the internet of things - for everyone!

=> Step 1: Get Information from sensor nodes (like temperature, led,...).
  - Step 2: Sensor nodes send data through Serial Write into Gateway(https://github.com/lequocanh662000/AI-IoT-Gateway) for process data (inclduing process AI model).   
  - Step 3: Gateway publish MQTT data onto Adafrui-Feed(maximum 1MB storage for free tier).
  - Finally: IOT_Adafruit_Application Subcribe data to the Adafruit-Feed by using Adafruit library for getting data automatically.
          OR Publish data to the Adafruit-Feed.

Extra Function: Handle Error Control (using ACK packets)
 
