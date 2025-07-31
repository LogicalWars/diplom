FROM node:18-alpine

ARG VUE_APP_BASE_URL

ENV VUE_APP_BASE_URL=$VUE_APP_BASE_URL

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

EXPOSE 8080

CMD ["npm", "run", "serve"]