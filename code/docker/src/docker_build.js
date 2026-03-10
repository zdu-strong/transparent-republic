const { execSync } = require("child_process")
const path = require('path')

async function main() {
    await buildReact();
    await buildSpringboot();
    await buildCloud();
}

async function buildReact() {
    execSync(
        [
            "docker build",
            "-t react",
            "-f ./Dockerfile",
            "../../../..",
        ].join(" "),
        {
            stdio: "inherit",
            cwd: path.join(__dirname, "./client"),
        }
    );
}

async function buildSpringboot() {
    execSync(
        [
            "docker build",
            "-t springboot",
            "-f ./Dockerfile",
            "../../../..",
        ].join(" "),
        {
            stdio: "inherit",
            cwd: path.join(__dirname, "./server"),
        }
    );
}

async function buildCloud() {
    execSync(
        [
            "docker build",
            "-t cloud",
            "-f ./Dockerfile",
            "../../../..",
        ].join(" "),
        {
            stdio: "inherit",
            cwd: path.join(__dirname, "./cloud"),
        }
    );
}

module.exports = main()