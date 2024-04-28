from flask import Flask, request, jsonify, render_template
import logging
from flask_pymongo import PyMongo
from bson.json_util import dumps

app = Flask(__name__)
app.config["MONGO_URI"] = "mongodb://localhost:27017/tweets"
mongo = PyMongo(app)
logging.basicConfig(level=logging.INFO)

@app.route("/", methods=["GET"])
def render_home():
  context = {
    "title": "Home"
  }
  return render_template("index.html.j2", **context)

@app.route("/top_users", methods=["GET"])
def get_top_users():
  top_pipeline = [
    {"$group": {"_id": "$user", "count": {"$sum": 1}}},
    {"$sort": {"count": -1}},
    {"$limit": 20}
  ]
  top_users = mongo.db.tweets.aggregate(top_pipeline)
  users_data = [user for user in top_users]
  return jsonify(users_data)

@app.route("/query_distribution", methods=["POST"])
def get_query_distribution():
  query = request.json.get('query', 'data')
  dist_pipeline = [
    {"$match": {"text": {"$regex": f".*{query}.*", "$options": "i"}}},
    {
      "$addFields": {
        "date_obj": {
          "$dateFromString": {
            "dateString": "$date",
            "format": "%Y-%m-%d"
          }
        }
      }
  },
  {
    "$group": {
      "_id": {
        "year": {"$year": "$date_obj"},
        "month": {"$month": "$date_obj"},
        "day": {"$dayOfMonth": "$date_obj"},
      },
      "count": {"$sum": 1}
    }
    },
    {"$sort": {"_id.year": 1, "_id.month": 1, "_id.day": 1}}
  ]
  queryDist = mongo.db.tweets.aggregate(dist_pipeline)
  dist_data = [data for data in queryDist]
  return jsonify(dist_data)

if __name__ == "main":
  app.run(debug=True)