from flask import Flask

from webapp.utils import cc_app_display_pages, mission_app_rest_calls, common_rest_calls

app = Flask(__name__, template_folder='templates', static_folder='static')

app.secret_key = "your_secret_key"  # Replace with a secure secret key..this key is used by flutter for session data enc

########################################################################
### These are the REST calls from Mission tracker flutter app
# Handle login request from flutter app
app.add_url_rule('/api/get_miss_det', view_func=mission_app_rest_calls.get_mission_details, methods=['POST'])

# Handle mission status update (inprogress,failed,success) request from flutter app
app.add_url_rule('/api/update_status', view_func=mission_app_rest_calls.update_mission_status, methods=['POST'])

# Handle curr location update  request from flutter app
app.add_url_rule('/api/update_loc', view_func=mission_app_rest_calls.update_mission_location, methods=['POST'])
########################################################################


########################################################################
### These are the routes  from CC app's UI (This app)

# Display home page
app.add_url_rule('/', view_func=cc_app_display_pages.home)

# Display and handle login process
app.add_url_rule('/login', view_func=cc_app_display_pages.login, methods=['POST', 'GET'])

# Display set lat lon page
app.add_url_rule('/cc/disp_set_latlon', view_func=cc_app_display_pages.disp_set_latlon_page)


# Display list apps page
app.add_url_rule('/cc/listappids', view_func=cc_app_display_pages.disp_appids_page)

# Display selected apps mission details like lat lon etc
app.add_url_rule('/cc/listappids/mission_details/', view_func=cc_app_display_pages.disp_appdetails_page)

# Display the add user form to add users to CC app
app.add_url_rule('/cc/add_user_form', view_func=cc_app_display_pages.add_user_form)
########################################################################

########################################################################
### These are the routes/REST calls common to from CC app's UI (This app) AND Flutter mobile app
# Handle  set lat lon submit
app.add_url_rule('/api/submit/set_latlon', view_func=common_rest_calls.set_latlon, methods=['POST'])
########################################################################


# Run the application
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
