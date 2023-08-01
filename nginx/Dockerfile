FROM nginx:1.22.0-alpine

# Add bash
RUN apk add --no-cache bash

# Set the name of the service
ENV UPSTREAM_HOST=user

# Copy the template file
COPY ./default.conf /etc/nginx/conf.d/default.conf.template

# Replace the variable at runtime and start Nginx
CMD /bin/bash -c "envsubst '\$UPSTREAM_HOST' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"
