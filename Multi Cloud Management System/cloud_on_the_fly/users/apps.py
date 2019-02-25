from django.apps import AppConfig


class UsersAppConfig(AppConfig):

    name = "cloud_on_the_fly.users"
    verbose_name = "Users"

    def ready(self):
        try:
            import users.signals  # noqa F401
        except ImportError:
            pass
