document.getElementById("sendEvent").addEventListener("click", async () => {
  const eventName = document.getElementById("eventName").value;
  if (!eventName) return alert("Please enter an event name!");

  await fetch("http://<PRODUCER_SERVICE_IP>:8080/send", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ event: eventName, timestamp: Date.now() })
  });

  addEventToList(eventName);
});

function addEventToList(event) {
  const list = document.getElementById("eventList");
  const li = document.createElement("li");
  li.textContent = `📩 ${event} @ ${new Date().toLocaleTimeString()}`;
  list.prepend(li);
}
