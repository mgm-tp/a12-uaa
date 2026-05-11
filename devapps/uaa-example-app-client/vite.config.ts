/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { viteStaticCopy } from "vite-plugin-static-copy";
import Path from "path";
import packageJson from "./package.json" with { type: "json" };

const rootPath = Path.join(import.meta.dirname, "..", "..");

const uaaResourcesPath = Path.join(
  rootPath,
  "node_modules",
  "@com.mgmtp.a12.uaa",
  "uaa-authentication-client",
  "resources",
);

const allDeps = Object.keys(packageJson.dependencies);

export default defineConfig({
  build: {
    outDir: "target/webpack",
    emptyOutDir: true,
    rollupOptions: {
      input: {
        main: Path.resolve(import.meta.dirname, "index.html"),
        composable: Path.resolve(import.meta.dirname, "composable.html"),
      },
    },
  },
  resolve: {
    dedupe: allDeps,
  },
  define: {
    SC_DISABLE_SPEEDY: "false",
    __VERSION__: JSON.stringify(packageJson.version),
    "process.env.npm_config_server_url": JSON.stringify(
      process.env.npm_config_server_url ?? "",
    ),
    "process.env.npm_config_idp_url": JSON.stringify(
      process.env.npm_config_idp_url ?? "",
    ),
    "process.env.npm_config_idp_realm": JSON.stringify(
      process.env.npm_config_idp_realm ?? "",
    ),
    "process.env.npm_config_idp_client_id": JSON.stringify(
      process.env.npm_config_idp_client_id ?? "",
    ),
  },
  plugins: [
    react(),
    viteStaticCopy({
      targets: [
        { src: Path.join(uaaResourcesPath, "*"), dest: "." },
      ],
    }),
  ],
  server: {
    host: "0.0.0.0",
    port: 3000,
    cors: { origin: "null", credentials: true },
  },
});
