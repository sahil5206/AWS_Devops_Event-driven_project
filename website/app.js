document.getElementById('send').addEventListener('click', async () => {
  const backendUrl = "http://producer-service:5000/produce";
  try {
    const res = await fetch(backendUrl, { method: "POST" });
    if (res.ok) alert("Event sent successfully!");
    else alert("Failed to send event");
  } catch (err) {
    alert("Error: " + err.message);
  }
});
