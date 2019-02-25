# Create your models here.

from django.db import models


class CloudFileSystem(models.Model):
    name = models.CharField(max_length=100000, blank=True, default='ram')
    file_location = models.CharField(max_length=100000, blank=True)
    aws_count = models.IntegerField(blank=True, default=0)
    azure_count = models.IntegerField(blank=True, default=0)
    gcp_count = models.IntegerField(blank=True, default=0)
