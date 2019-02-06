package org.brightify.reactant.rx;

import io.reactivex.subjects.Subject;

public class OptionalSubjectWorkaround {

    public static <T> void onNextWorkaround(Subject<T> subject, T nextValue) {
        subject.onNext(nextValue);
    }

}
