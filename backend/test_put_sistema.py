import requests
import json

# Authenticate
auth_url = 'http://localhost:8080/api/authenticate'
auth_payload = {'username': 'admin', 'password': 'admin'}
response = requests.post(auth_url, json=auth_payload)
token = response.json().get('id_token')

if not token:
    print("Failed to get token")
    exit(1)

headers = {'Authorization': f'Bearer {token}', 'Content-Type': 'application/json'}

# Get a system
get_url = 'http://localhost:8080/api/_search/sistemas?page=0&size=1'
res = requests.get(get_url, headers=headers)
if res.status_code != 200 or not res.json():
    print("Could not fetch sistema")
    print(res.text)
    exit(1)

sistema = res.json()[0]

# PUT /api/sistemas
put_url = 'http://localhost:8080/api/sistemas'
sistema['nome'] = sistema['nome'] + ' test'
print("Putting sistema...")
put_res = requests.put(put_url, headers=headers, json=sistema)

print(f"Status Code: {put_res.status_code}")
if put_res.status_code == 500:
    print("Server Error Details:")
    print(put_res.text)
else:
    print("Success or other status:")
    print(put_res.text)
