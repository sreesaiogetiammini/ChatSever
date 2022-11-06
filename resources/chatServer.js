"use strict"
console.log("In Js")
function main(){
    console.log("Inside Main Javascript")
    let ws = new WebSocket("ws://192.168.4.23:8080");
    ws.onopen= ()=>{
        console.log("Inside Websocket Open");
    };
    ws.onerror = handleErrorCB;

}

function handleErrorCB(){
    console.log("Inside Websocket Error");
}

window.onload = main;