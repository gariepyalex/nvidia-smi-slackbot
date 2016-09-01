# nvidia-smi-slackbot

This slack bot is intended to be used on shared servers where Nvidia GPUs are used for scientific calculations and shared among many peoples. The bot will notify when the GPUs are being used, providing the name of the users, the memory used and the process PIDs.

## Usage

1. Ensure that nvidia-smi is installed.
2. Add an incoming webhook to your slack channel https://cobralaval.slack.com/apps/new/A0F7XDUAZ-incoming-webhooks.
3. `cp resources/config.template.clj resources/config.clj`.
4. Edit `resources/config.clj` with the URL of your own slack webhook.
5. `lein run`
