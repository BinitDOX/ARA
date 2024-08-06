# ARA (Active Recall Assistant)
ARA is a Personalized AI Assistant for Active Recall and Automation. It empowers you to manage your (Android) device and access information with a customizable, multi-assistant interface.
- Enhanced Productivity: Execute commands and control your device directly through ARA, streamlining workflows and minimizing manual interaction.
- Tailored Assistance: Create and personalize multiple AI assistants, each with distinct voices and functionalities. This allows you to configure them for specific tasks or preferences.
- Active Recall Focus: ARA prioritizes prompting you with information to strengthen memory and recall, supporting a more knowledge-centric approach to AI interaction.

## Demo Video:
[![DEMO VIDEO](https://github.com/BinitDOX/ARA/assets/93908298/9eafd045-4846-4308-9739-09ec384f960d)](https://youtu.be/lnF41vxvm4w)

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
4. The URL should look something like: 'stallion-uncommon-exact.ngrok-free.app'<br/>(Do not copy the 'http://' prefix or the '/' suffix)
5. Go to <a href="https://www.kaggle.com/code/yeeandres/ara-server">this</a> notebook and click 'Copy & Edit'
6. Set the accelerator as GPU P100 under notebook options if not already selected.
7. Set the persistence to Files Only under notebook options if not already selected.
8. Set the NGROK_AUTHTOKEN under 'Constants' with the auth token in Step-2.
9. Set the NGROK_STATIC_DOMAIN under 'Constants' with the static domain in Step-3.
10. Click Run All and wait for ~15m for the first run. Successive runs will take ~5min, if file persistence is on.
11. It will/may get stuck while downloading RVC Assets. (~6th cell under 'SETUP')
12. When the last log under this cell states: "all download taks finished", just 'Cancel' and 'Run All' again (~5min).
13. Under 'EXECUTE' section you should see server logs like: "Started server process", after the start-up.
14. You can verify if the server has started by opening the base backend URL from Step-3 in the browser.
15. If all is done correctly, you should see something like {"detail":"Not found"}
16. Use the notebook in this interactive mode only. If you 'Save and commit', the file persistence (conversation data) is lost.

## Application Setup Instructions:
0. Watch and follow the <a href="https://youtu.be/lnF41vxvm4w">demo</a>.
1. You can either clone the repository and build the app yourself using android studio, OR
2. Goto <a href="https://github.com/BinitDOX/ARA/releases">releases</a> and download the latest version of the app.
3. Install and grant the necessary permissions, specially the accessibility permission.
4. Tap the 3-dot Menu, goto Settings.
5. Set the Base URL (Step-3), with the 'https://' prefix and '/' suffix.<br/>(Ex: https://stallion-uncommon-exact.ngrok-free.app/)
6. If you built the app yourself, you can just set this as BASE_URL in the dev.properties file.
7. Check out and set the rest of the settings as you please and save.
8. Then tap the Add Assistant Button, on the bottom right.
9. Before filling in the assistant details, tap on 'Edge Voices' drop-down menu.
10. If you do see a list of voices then you are connected to the server, if not check the logs for info.
11. Customize the assistant as you see fit:
    - Image: Assistant's profile image
    - Name: Name of the assistant (Ex: Jarvis)
    - About: Tiny description of the assistant
    - Color: Chat bubble title color of the assistant
    - Edge Voice: This is TTS (Text-To-Speech), provided by microsoft. <a href="https://pypi.org/project/edge-tts/">Usage</a>.
    - Edge Voice Pitch: To set a custom pitch (Use this to fine-tune an RVC model)
    - RVC Voice: (Optional) (STS) This converts a TTS audio output to any available custom RVC voice model (easily extendable)
    - Using the above three voice options, you have a huge amount of voice options.
    - Instruction prompt: This is the main intruction prompt for the assistant, you would usually edit only instructions 1 and 2. But feel free to edit or add more if the assistant is not up to the mark.
12. Swipe to save, click the newly created assistant.
13. Set it as a default assistant from the 3-dot menu.
14. Chat and enjoy!.


## Extra Notes:
- In a chat, the button on left of camera can be used to test commands. Choose any command, edit the arguments and send it.
- Basically, anything after the &lt;TEST&gt; token is completely ignored by the assistant.
- Long press a message to see some useful options, like copy, reply, delete, etc. (Specially delete, to clean up a bad response)
- Append the &lt;BREAK&gt; token at the end of your message to NOT get a response from the assistant. Useful when you want to send multiple messages.
- The assistant may use the &lt;BREAK&gt; token too. (Limited by the system to ~2 at once)
- A default assistant needs to be set to handle device events like incoming call, alarms etc.
- Deleting a chat also deletes the assistant.
- Enable google assistant override in settings, it's just too good!

## Privacy Details:
Although none of this should matter as any production app also is hosted with external services anyway, still:
Anyhing that you share with the assistant, goes through:
- Ngrok: Used for private to public IP forwarding with HTTPS encrypted traffic. Private HTTP -> Public HTTPS.
Can easily be solved by creating a self-signed SSL certificate and setting it in the 'config' of fastAPI. (Private HTTPS -> Public HTTPS)
- Kaggle: Although receives HTTPS, but has the database and conversation file.
Can be solved by storing encrypted data.
- Microsoft: For TTS (Text-To-Speech) API service.
Can be solved by using a custom TTS model (SO-VITS)
- Google: For voice input recognition.
Can be solved by using Whisper voice recognition model on the server.

Therefore, none of the critical private info is shared from the app, with the assistant like, payment codes, contact number etc.


## Credits:
<i>I don't remember..., I'll add as I remember...</i>
- <a href="https://github.com/SmartToolFactory/Flexible-Chat-Box">Flexible Chat Box</a>
- <a href="https://huggingface.co/TheBloke/dolphin-2.1-mistral-7B-GPTQ">LLM Model</a>
- <a href="https://github.com/skydoves/colorpicker-compose">Color Picker</a>
- <a href="https://medium.com/make-apps-simple/swipe-button-using-jetpack-compose-bd50a824d8cc">Swipe Button</a>
