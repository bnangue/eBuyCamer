<?php

  
$message = $_POST['message'];

$sender = $_POST["sender"];
$receiver = $_POST["receiver"];

$receivertoken = $_POST['receivertoken'];

$foreign_uid = $_POST['foreign_uid'];
$title = $_POST["title"];

$post_id = $_POST['post_id'];
$creator_uid = $_POST["creator_uid"];
$is_deal = $_POST["is_deal"];
$sender_uid = $_POST["sender_uid"];

$apiKey ='AAAApknq7Pw:APA91bFuuVyG1O7VGmzNG3R8Fdfk-4eZjCPmV6Le3dQzJ32LDJ_KrpWdkYeyfTKG5BIX8vAhlk9Wcs3-G-35A_kGSlHhA8zslX4zzVSugyHnGHIY6TU3Pch7qpBh0aR1k8QQzPHvNzA5JO3gc3KRnJQWUM_3R_rs4w';

$url = 'https://fcm.googleapis.com/fcm/send';
 
$fields = array(
                'registration_ids'  => array( $receivertoken ),
                'data'              => array( "message" => $message ,"sender" => $sender,"receiver" => $receiver, "sender_uid" => $sender_uid,
                	"foreign_uid"=>$foreign_uid, "title" => $title, "post_id" => $post_id, "creator_uid" => $creator_uid, "is_deal" => $is_deal)
                );
 
$headers = array( 
                    'Authorization: key=' . $apiKey,
                    'Content-Type: application/json'
                );
 

// Open connection
$ch = curl_init();
 
// Set the url, number of POST vars, POST data
curl_setopt( $ch, CURLOPT_URL, $url );
 
curl_setopt( $ch, CURLOPT_POST, true );
curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
curl_setopt( $ch, CURLOPT_SSL_VERIFYHOST, 0);
curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, false);
 
curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $fields ) );
 
// Execute post
$result = curl_exec($ch);
 
// Close connection
curl_close($ch);
 
echo $result;
 
?>