console.log("script.js loaded");

const pieChartElm = document.getElementById("pie-chart");
const pieChart = new Chart(pieChartElm, {
  type: "pie",
  data: {
    labels: ["loading..."],
    datasets: [
      {
        label: "retweets",
        data: ["loading..."],
        backgroundColor: [
          "red",
          "blue",
          "green",
          "yellow",
          "orange",
          "purple",
          "pink",
          "brown",
          "grey",
          "cyan",
          "magenta",
        ],
        borderWidth: 1,
      },
    ],
  },
});

const trendChartElm = document.getElementById("trend-chart");
const trendChart = new Chart(trendChartElm, {
  type: "line",
  data: {
    labels: ["loading..."],
    datasets: [
      {
        label: "frequency",
        data: ["loading..."],
        borderWidth: 1,
      },
    ],
  },
});

let query = "will";

const intervalId = setInterval(() => {
  fetch("/top_users")
    .then((response) => response.json())
    .then((data) => {
      pieChart.data.labels = data.map((user) => user._id);
      pieChart.data.datasets[0].data = data.map((user) => user.count);
      pieChart.update();
    })
    .catch((error) => {
      console.error("Error:", error);
    });

  getDistripbution();
}, 5000);

const getDistripbution = () => {
  fetch("/query_distribution", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ query }),
  })
    .then((response) => response.json())
    .then((data) => {
      trendChart.data.labels = data.map((dist) => `day-${dist._id.day}`);
      trendChart.data.datasets[0].data = data.map((dist) => dist.count);
      trendChart.update();
    })
    .catch((error) => {
      console.error("Error:", error);
    });
};

document
  .getElementById("query-form")
  .addEventListener("submit", function (event) {
    event.preventDefault(); // prevent the form from submitting normally
    let userInput = document.getElementById("query").value;
    if (userInput) {
      query = userInput;
      getDistripbution();
    }
  });
