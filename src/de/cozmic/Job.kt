package de.cozmic

class Job(val prepareTime: Float, val price: Float) {
    var isNew = true
    var isCreated = false
    var remainingTime = prepareTime
}