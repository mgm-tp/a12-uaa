# UAA Language Server Prototype

1. Build by npm clean && npm install && npm compile
2. Install plugin lsp4ij from Redhat version 0.0.2 - Properly 0.0.3 will support highlight.
3. Open src/authorization.json which is prepared for testing the editor.

Intellij:

3. Follow guideline from the plugin input these 2 lines in server command:
   4. Server tab:
      sh -c "(`__PATH__`)/uaa-authorization-language-server/main/uaa-authorization-language-server --stdio"
      sh -c "(`__PATH__`)/uaa-authorization-language-server/main/uaa-authorization-language-server --node-ipc"
   4. Mappings tab: In Mappings please configure your specific json file for testing. Language ID is "json".
   5. Debug tab: Configure error reporting as in long and trace as verbose.

VSCode:
1. Install plugin https://marketplace.visualstudio.com/items?itemName=llllvvuu.llllvvuu-glspc name "Generic LSP Client"
2. press command + shilf + x please transfer to your windows.
3. Choose the plugin 
4. Press the Setting icon (Enable/Disable - Install/Uninstall - Setting icon)
5. Choose Extension Settings.
6. Input server command as below: (`__PATH__`)/uaa-authorization-language-server/main/uaa-authorization-language-server
7. Language id: json
8. Server command arguments: "--stdio",
   "--node-ipc"
Your setting as json look like this:
9. "glspc.serverCommandArguments": [
   "--stdio",
   "--node-ipc"
   ],
   "glspc.serverCommand": "(`__PATH__`)/uaa-authorization-language-server/main/uaa-authorization-language-server",
   "workbench.settings.applyToAllProfiles": [
   "glspc.serverCommand",
   "glspc.languageId",
   "glspc.serverCommandArguments"
   ]

Eclipse/Spring Tool Suite:
1. Eclipse has built-in support for LSP therefore you don't need to install anything.
2. Create a external tool configuration 
3. Choose new program
4. Location: (`__PATH__`)/uaa-authorization-language-server/main/uaa-authorization-language-server
5. working directory: choose your directory.
6. Argument: --stdio
7. Open your Eclipse setting -> Language server
   8. Uncheck everything there.
   9. Press "..add" button.
   10. On the left side of panel choose JSON.
   11. On the right side of panel choose the program which you did in step 2 - 6
   12. Open json file. if it does not work please open workspace and open a json file.
   13. You should see the logging of LSP which are print out.

* - (`__PATH__`): Path to uaa project in local

Enjoy our Editor!

> **_NOTE:_**  Our code includes 2 files `languageModelCache.ts` and `runner.ts`, which are part of the VSCode codebase. We need to check whether the license for this code is being used or not.\
> Link: https://github.com/microsoft/vscode/blob/main/extensions/json-language-features/server/src/languageModelCache.ts \
> Link: https://github.com/microsoft/vscode/blob/main/extensions/json-language-features/server/src/utils/runner.ts




