package services

import "go.mongodb.org/mongo-driver/v2/mongo"

var mongoClient *mongo.Client
var msgCollection *mongo.Collection
