<!DOCTYPE html>
<html>
<head>
	<script>
		if (window.top !== window) {
			//running in iframe, reload top window
			window.top.location.reload();
		} else {
			//just change location
			window.location = 'ui'
		}
	</script>
	<meta http-equiv="refresh" content="2;URL='ui'" />
</head>
</html>
