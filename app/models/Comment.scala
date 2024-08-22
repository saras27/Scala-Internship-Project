package models

import java.sql.Timestamp

case class Comment(
                    commentId : Long,
                    userId : Long,
                    postId : Long,
                    comment : String,
                    datePosted : Timestamp)
