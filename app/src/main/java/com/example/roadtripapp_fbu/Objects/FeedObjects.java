package com.example.roadtripapp_fbu.Objects;

import java.util.Date;

/**
 * Interface for adding multiple itemviews into user's Trip Feed
 */
public interface FeedObjects {
    int TYPE_JOURNAL = 101;
    int TYPE_POST = 102;

    int getType();
}
