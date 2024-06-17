# ARA (Active Recall Assistant)
ARA is a Personalized AI Assistant for Active Recall and Automation. It empowers you to manage your (Android) device and access information with a customizable, multi-assistant interface.
- Enhanced Productivity: Execute commands and control your device directly through ARA, streamlining workflows and minimizing manual interaction.
- Tailored Assistance: Create and personalize multiple AI assistants, each with distinct voices and functionalities. This allows you to configure them for specific tasks or preferences.
- Active Recall Focus: ARA prioritizes prompting you with information to strengthen memory and recall, supporting a more knowledge-centric approach to AI interaction.


## Demo Video:



## Notes:
This is a successor project to <a href="https://github.com/BinitDOX/AI-YFU">AI-YFU</a>.

### Pros:
- App is built with the latest production-level android tech stack.
- Server is (self) hosted on kaggle. (May be hosted locally with 10+GB GPU)
- Multiple assistants can be created, each with different personality and voice.
- Each assistant can initiate conversations seemlessly.
- Can handle voice inputs and outputs, even in background or locked device.
- Can reply to previous messages.
- Can send multiple messages at once.
- Can override google assistant for handsfee interaction.
- Each assistant supports long-term memory for active recall by using <a href="https://github.com/jdagdelen/hyperDB/tree/main">HyperDB</a>.
- Can intercept and handle device events, like calls, alarms etc. (easily extendable)
- Can execute commands and control the device to a certain extent (easily extendable)
- Server can handle multiple client applications, with authentication.
- Is blazingly fast and only uses ~10GB GPU VRAM out of 16GB.

### Cons:
- It cannot handle images yet. (TODO)
- Still uses an old LLM model. (updatable)
- Kaggle only provides ~30hr GPU weekly/account, so limited hosting.

## Server Setup Instructions:
1. Make a <a href="https://www.kaggle.com/">kaggle</a> account and verify using phone to get ~30hrs of weekly GPU.
2. Make an <a href="https://ngrok.com/">ngrok</a> account and get your auth token from <a href="https://dashboard.ngrok.com/get-started/your-authtoken">here</a>.
3. Also create a free static domain from <a href="https://dashboard.ngrok.com/cloud-edge/domains">here</a> and copy the URL.
4. Go to <a href="https://www.kaggle.com/code/yeeandres/ara-server">this</a> notebook and click 'Copy & Edit'
5. Set the accelerator as GPU P100 under notebook options if not already selected.
6. Set the persistence to Files Only under notebook options if not already selected.
7. Set the NGROK_AUTHTOKEN under 'Constants' with the auth token in Step 2.
8. Set the NGROK_STATIC_DOMAIN under 'Constants' with the static domain in Step 3.
9. Click Run All and wait for ~15m for the first run. Successive runs will take ~5min, given that file persistence is on.
10. It may get stuck while downloading RVC Assets. If the logs under this cell states: "all download taks finished", just 'Cancel' and 'Run All' again.
11. Under 'EXECUTE' section you should see server logs like: "Started server process", after the start-up.
12. You can verify if the server has started correctly by openening the base backend URL from Step 4 in the browser.
13. If all is done correctly, you should see something like {"detail":"Not found"}
14. Use the notebook in this interactive mode only. If you 'Save and commit', the file persistence (conversation data) is lost.

// Add more later

