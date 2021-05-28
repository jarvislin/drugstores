package com.jarvislin.domain.entity

class InvalidReportTimeException : Exception("Too frequent")
class RssException : Exception("Error occurred when RSS parser executed.")
class ContentNotFoundException : Exception("Error occurred when CSV file has no data.")
class HeaderNotFoundException : Exception("Error occurred when CSV file doesn't have specific fields.")