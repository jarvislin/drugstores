package com.jarvislin.domain.entity

class InvalidReportTimeException : Exception("Too frequent")
class RssException : Exception("Error occurred when RSS parser executed.")