// Producer endpoint is exposed via the website pod (Nginx proxy to the cluster service)
const PRODUCER_API =
  window.PRODUCER_API_ENDPOINT || "/api/send";

let totalEvents = 0;
let uniqueEvents = new Set();
let activeUsers = 0;

let eventCount = {
  page_view: 0,
  purchase: 0,
  add_to_cart: 0,
  logout: 0,
  feedback: 0,
};

// Chart setup
const ctx = document.getElementById("eventChart").getContext("2d");
const eventChart = new Chart(ctx, {
  type: "bar",
  data: {
    labels: Object.keys(eventCount),
    datasets: [
      {
        label: "Event Count",
        data: Object.values(eventCount),
        backgroundColor: "#2563eb",
      },
    ],
  },
  options: {
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  },
});

document.querySelectorAll(".event-btn").forEach((btn) => {
  btn.addEventListener("click", async () => {
    const eventType = btn.dataset.event;
    const eventPayload = {
      eventType,
      timestamp: new Date().toISOString(),
      user: `user_${Math.floor(Math.random() * 1000)}`,
      sessionId: Math.random().toString(36).substring(7),
    };

    logEvent(`📤 Sending: ${eventPayload.eventType}`);

    try {
      const res = await fetch(PRODUCER_API, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(eventPayload),
      });

      if (res.ok) {
        logEvent(`✅ ${eventPayload.eventType} event sent`);
        updateMetrics(eventPayload);
      } else {
        logEvent(`❌ Failed to send event: ${res.status}`);
      }
    } catch (err) {
      logEvent(`⚠️ Network Error: ${err.message}`);
    }
  });
});

function logEvent(msg) {
  const logDiv = document.getElementById("log");
  const entry = document.createElement("p");
  entry.textContent = `[${new Date().toLocaleTimeString()}] ${msg}`;
  logDiv.appendChild(entry);
  logDiv.scrollTop = logDiv.scrollHeight;
}

function updateMetrics(payload) {
  totalEvents++;
  uniqueEvents.add(payload.eventType);
  activeUsers = Math.min(10, activeUsers + 1);

  eventCount[payload.eventType]++;
  eventChart.data.datasets[0].data = Object.values(eventCount);
  eventChart.update();

  document.getElementById("totalEvents").textContent = totalEvents;
  document.getElementById("uniqueEvents").textContent = uniqueEvents.size;
  document.getElementById("activeUsers").textContent = activeUsers;

  // Auto decrease active users after delay
  setTimeout(() => {
    activeUsers = Math.max(0, activeUsers - 1);
    document.getElementById("activeUsers").textContent = activeUsers;
  }, 15000);
}
