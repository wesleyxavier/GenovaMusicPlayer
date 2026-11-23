FROM python:3.12-slim

WORKDIR /e2e

COPY requirements.txt /e2e/requirements.txt
RUN pip install --no-cache-dir -r /e2e/requirements.txt

COPY . /e2e

CMD ["pytest", "-v"]
