"use strict"
let userNames_ = [];
let messagesSent_ = [];
let userNameInput;
let chatRoomInput;
let userNamesTextArea;
let messagesTextArea;
let messageInput ;
let joinRoomBtn;
let wsOpen = false;
let ws ;
function addUserNamesToDiv(){
    const title = document.createElement("h2");
    const node = document.createTextNode("People in a chat room");
    title.appendChild(node);
    userNamesTextArea.value = title.innerText;
    userNames_.forEach(ele =>{
        userNamesTextArea.value +='\r\n' + ele;
    })
}

function addMessageToTextBox(){
    messagesTextArea.value = "";
    messagesSent_.forEach(ele =>{
        console.log(ele);
        messagesTextArea.value +='\r\n' + ele;
    })
}


function validateRoomName(roomName){
    for(let i =0; i<roomName.length;i++){
        if(roomName.charCodeAt(i)<97){
            return false;
        }
        else if(roomName.charCodeAt(i)>123){
            return false;
        }
    }
    return true;
}

function handleKeyPressCB(){
    console.log("handling join room key press");
    let userNameInputValue = userNameInput.value;
    let chatRoomInputValue = chatRoomInput.value;
    if(userNameInputValue.length ==0){
        alert("Please make sure you entered a userName");
        userNameInput.select()
        return;
    }
    if(!validateRoomName(chatRoomInputValue)){
        alert("Please make sure you entered a valid chatroom\n Note: Valid room names contain only lowercase letters (and no spaces)");
        chatRoomInput.select()
        return;
    }

    if(!(userNames_.includes(userNameInputValue))){
        let userJson = {};
        userJson.type = 'join';
        userJson.user = userNameInputValue;
        userJson.room = chatRoomInputValue;
        ws.send(userJson.type+" "+userJson.user+" "+userJson.room);
        // if(wsOpen){
        //     ws.send(userJson.type+" "+userJson.user+" "+userJson.room);
        // }
        // else {
        //     console.log(wsOpen);
        //     handleWSErrorCB();
        // }
    }
    else {
        alert("This User is already exists in this chat room");
    }
    messageInput.disabled = false;
}


function doit() {

    console.log("doit");
    ws = new WebSocket("ws://localhost:8080");
    ws.onopen = handleWSOpenCB;
    ws.onerror = handleWSErrorCB;
    ws.onmessage = handleWSMessageCB;
    ws.onclose = handleWSClose;

}

function main()
{
    document.body.onclick = doit;

    // ws = new WebSocket("ws://localhost:8080/abc");
    // ws.onopen = handleWSOpenCB;
    // ws.onerror = handleWSErrorCB;
    // ws.onmessage = handleWSMessageCB;
    // ws.onclose = handleWSClose;

    userNameInput = document.getElementById("userNameId");
    chatRoomInput = document.getElementById("chatRoomId");
    userNamesTextArea = document.getElementById("userNameBoxId");
    messagesTextArea = document.getElementById("messageBoxId");
    messageInput = document.getElementById("inputMessageBoxId");
    messageInput.disabled = true;
    joinRoomBtn = document.getElementById("joinRoomBtn");
    userNameInput.value = "SreeSai";
    chatRoomInput.value = "testroom";

    joinRoomBtn.addEventListener("click",handleKeyPressCB);
   // messageInput.addEventListener("keypress", handleSendMessageBtn);

}

function handleWSOpenCB(){
    console.log("WebSocket Opened")
    wsOpen = true;
}

function handleWSErrorCB( e ){
    console.log("Error Occurred in WebSocket ", e);
    wsOpen = false;
}

function handleWSMessageCB(event){
    console.log("Inside WebSocket Message Handling");
    let obj = JSON.parse(event.data);
    console.log(obj.type);
    if(obj.type == 'join'){
        if(!userNames_.includes(obj.user)){
            userNames_.push(obj.user);
        }
        if(!messagesSent_.includes(obj.user+" has entered the room")){
            messagesSent_.push(obj.user+" has entered the room");
        }
        addUserNamesToDiv();
        addMessageToTextBox();
    }

    else if(obj.type =='message'){
        console.log("Inside Message");
        if(!messagesSent_.includes(obj.user+" : "+obj.message)){
            messagesSent_.push(obj.user+" : "+obj.message);
        }
        addUserNamesToDiv();
        addMessageToTextBox();
    }

    else if(obj.type =='leave'){
        console.log("Inside Leave");
        userNames_ = userNames_.filter(v => v !== obj.user);
        messagesSent_.push(obj.user+" has left the room");
        addUserNamesToDiv();
        addMessageToTextBox();
        // handleWSClose();
    }


}

function handleWSClose(){
    wsOpen = false;
    console.log("WebSocket is closed");
}

window.onload= main;
