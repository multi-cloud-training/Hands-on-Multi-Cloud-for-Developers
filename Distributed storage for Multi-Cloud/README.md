# multicloudstorage
We intend to provide a distributed multicloud storage by splitting ng the data b/w n cloud providers and retrieving it immediately using n-1 clouds, this would enable the users to choose the vendors as per their requirement, without any need for data migration or replication.
We achieve this by following a RAID-5 based approach thereby inccoring an additional cost of just 1/(n-1) part of whole data.
With a multicloud strategy, users need not be locked on to a single cloud vendor and leverage more flexibility.



```
Development Stack

Frontend: HTML
Backend: Django
```

# Steps to run the software

```
pip install -r requirements.txt
python manage.py runserver
http://localhost:8000/
```
# Major API's
```
The Universal Upload API
The Universal Download API
```

# Cloud Services used
```
The Google Cloud Platform
Amazon Web Services
Microsoft Azure
```

