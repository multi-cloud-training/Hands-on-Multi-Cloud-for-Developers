import json
import unittest

app = __import__('D:/SJSU Study Docs/MS SE TermWise/Fall 2018/CMPE 226/Project Github/cmpe226project2/CMPE 226 Project/CMPE 226 Project')

app.testing = True


class TestApi(unittest.TestCase):

    # user
    def test_signup(self):
        with app.test_client() as client:
            try:
                # send data as POST form to endpoint
                sent = {'inputName': 'Ramu', 'inputEmail': 'ramu@gmail.com', 'inputPassword':'pass', 'inputBankAccount':987654321987}
                result = client.post(
                    '/signUp?inputRole=customer&inputCaId=123',
                    data=sent
                )
                # check result from server with expected data
                print(result.data)
                self.assertEqual(
                    result.data,
                    json.dumps({'message': 'User created successfully !'})
                )
            except Exception as e:
                print(str(e))

    # user
    def test_login(self):
        with app.test_client() as client:
            try:
                # send data as POST form to endpoint
                sent = {'inputEmailLogin': 'ramu@gmail.com', 'inputPasswordLogin':'pass'}
                result = client.post(
                    '/signUp?inputRole=customer',
                    data=sent
                )
                # check result from server with expected data
                print(result.data)
                self.assertEqual(
                    result.data,
                    json.dumps([11243, 'ramu@gmail.com', 'ramu', 'pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e', 987654321987])
                )
            except Exception as e:
                print(str(e))

        # csp
        def test_signup(self):
            with app.test_client() as client:
                try:
                    # send data as POST form to endpoint
                    sent = {'inputName': 'AWS', 'inputEmail': 'AWS@AWS.com', 'inputPassword': 'AWSPass',
                            'inputBankAccount': 789456123456}
                    result = client.post(
                        '/signUp?inputRole=csp&inputCaId=123',
                        data=sent
                    )
                    # check result from server with expected data
                    print(result.data)
                    self.assertEqual(
                        result.data,
                        json.dumps({'message': 'User created successfully !'})
                    )
                except Exception as e:
                    print(str(e))

        # csp
        def test_login(self):
            with app.test_client() as client:
                try:
                    # send data as POST form to endpoint
                    sent = {'inputEmailLogin': 'AWS@AWS.com', 'inputPasswordLogin': 'AWSpass'}
                    result = client.post(
                        '/signUp?inputRole=csp',
                        data=sent
                    )
                    # check result from server with expected data
                    print(result.data)
                    self.assertEqual(
                        result.data,
                        json.dumps([1234, 'AWS@AWS.com', 'AWS',
                                    'pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e',
                                    789456123456])
                    )
                except Exception as e:
                    print(str(e))

