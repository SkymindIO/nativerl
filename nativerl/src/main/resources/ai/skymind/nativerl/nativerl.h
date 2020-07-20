#ifndef NATIVERL_H
#define NATIVERL_H

#include <vector>

/**
 * This is the main C++ interface implemented, for example, in Java via JavaCPP,
 * and used in Python by, for example, RLlib via pybind11.
 */
namespace nativerl {

/**
 * A generic multidimensional array of 32-bit floating point elements with a very simple interface
 * such that it can be mapped and used easily with tools like JavaCPP and pybind11.
 */
class Array {
public:
    float* allocated;
    float* data;
    std::vector<ssize_t> shape;

    Array() : data(nullptr) { }
    Array(const Array &a)
            : allocated(nullptr), data(a.data), shape(a.shape) { }
    Array(float *data, const std::vector<ssize_t>& shape)
            : allocated(nullptr), data(data), shape(shape) { }
    Array(const std::vector<ssize_t>& shape)
            : allocated(nullptr), data(nullptr), shape(shape) {
        allocated = data = new float[length()];
    }
    ~Array() {
        delete[] allocated;
    }

    ssize_t length() {
        ssize_t length = 1;
        for (size_t i = 0; i < shape.size(); i++) {
            length *= shape[i];
        }
        return length;
    }
};

class Continuous;
class Discrete;

/** Base class for the Continuous and Discrete classes. */
class Space {
public:
    virtual ~Space() { };
    Continuous* asContinuous();
    Discrete* asDiscrete();
};

/**
 * Describes a continuous space for both state and action spaces. Includes low and high
 * values for all elements, as well as the shape of the Array required by the Environment.
 */
class Continuous : public Space {
public:
    std::vector<float> low;
    std::vector<float> high;
    std::vector<ssize_t> shape;

    Continuous(const std::vector<float>& low,
               const std::vector<float>& high,
               const std::vector<ssize_t>& shape)
            : low(low), high(high), shape(shape) { }
};

/**
 * Describes a discrete space for action spaces.
 * Includes the number of actions supported by the Environment.
 */
class Discrete : public Space {
public:
    ssize_t n;
    ssize_t size;

    Discrete(const Discrete& d) : n(d.n), size(d.size) { }
    Discrete(ssize_t n, ssize_t size = 1) : n(n), size(size) { }
};

/** Helper method to cast dynamically a Space object into Continuous. */
Continuous* Space::asContinuous() { return dynamic_cast<Continuous*>(this); }
/** Helper method to cast dynamically a Space object into Discrete. */
Discrete* Space::asDiscrete() { return dynamic_cast<Discrete*>(this); }

/**
 * The pure virtual (abstract) interface of a "native" environment. This gets mapped,
 * for example, with JavaCPP and implemented by a Java class. The implementation needs
 * to export functions to create and release Environment objects. In the case of JavaCPP,
 * the createEnvironment() and releaseEnvironment() are available in the generated
 * jniNativeRL.h header file.
 * <p>
 * However, we can just as well implement it in pure C++, which we would do in the case of,
 * for example, ROS or MATLAB Simulink.
 * <p>
 * On the Python side, these functions are picked up by, for example, pybind11 and used
 * to implement Python interfaces of environments, such as gym.Env, for RLlib, etc.
 */
class Environment {
public:
    virtual ~Environment() { };
    //    /** Passes a new random seed that should be used for reproducibility. */
    //    virtual void setSeed(long long seed) = 0;
    /** Returns the action Space supported. */
    virtual const Space* getActionSpace() = 0;
    /** Returns the action mask Space supported. */
    virtual const Space* getActionMaskSpace() = 0;
    /** Returns the observation Space supported. */
    virtual const Space* getObservationSpace() = 0;
    /** Returns the current state of the possible actions. */
    virtual const Array& getActionMask() = 0;
    /** Returns the current state of the simulation. */
    virtual const Array& getObservation() = 0;
    /** Indicates when a simulation episode is over. */
    virtual bool isDone() = 0;
    /** Used to reset the simulation, preferably starting a new random sequence. */
    virtual void reset() = 0;
    /** Can be used to run the simulation for a single agent with a discrete action space. */
    virtual float step(const Array& action) = 0;
    /** Can be used to run the simulation for a continuous action space and/or with multiple agents. */
//    virtual const Array& step(const Array& action) = 0;
    /** Returns the last values of observationForReward() */
    virtual const Array& getMetrics() = 0;
};

// typedef Environment* (*CreateEnvironment)(const char* name);
// typedef void (*ReleaseEnvironment)(Environment* environment);

}

#endif // NATIVERL_H
