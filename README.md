# NativeRL

## Introduction

NativeRL enables you to apply Reinforcement Learning (RL) to [AnyLogic](https://www.anylogic.com/) or Python-based simulations. We have optimized NativeRL to support almost all industrial use cases in verticals such as supply chain, manufacturing and many more.

### Example Simulations Trained Using NativeRL

- Product Delivery - https://cloud.anylogic.com/model/23213598-1bc4-419a-ade2-3b0e8a220e0d
- Flexible Manufacturing - https://cloud.anylogic.com/model/9044fa72-c5dd-4079-9f6a-a877464b905f
- AGVs - https://cloud.anylogic.com/model/bc88f3be-b5a6-4962-abc1-a26def59994d
- Mine Pit Operations - https://cloud.anylogic.com/model/a5c49106-d286-4f2a-9723-83d4a996672b8

## Getting Started

To get started, please navigate to our wiki (coming soon). There are generally 3 steps to get started.

1. Build the NativeRL libraries.
2. Install NativeRL locally and add Pathmind Helper to your AnyLogic simulation.
3. Train your Policy.

## Supported Features

**RL Algorithms**

Currently, NativeRL only supports PPO. From our experience, PPO works successfully for all use cases so there has not been a need to experiment with other RL algorithms.

**Hyperparameter Tuning**

Both PBT and PB2 are supported out-of-box. From our experience, PBT more consistently converges as compared to PB2. As such, PBT is the default trial scheduler in NativeRL.

| Hyperparameter Tuning Algorithm | Background |
|---------------------------------|------------|
| Population-Based Training (PBT) | https://www.deepmind.com/blog/population-based-training-of-neural-networks      |
| Population-Based Bandits (PB2)  | https://www.anyscale.com/blog/population-based-bandits       |

**RL Implementation**

All features below can be mixed and matched to fit your specific use case. For more information about multi-agent implementations, please see https://bair.berkeley.edu/blog/2018/12/12/rllib/.

| Feature                      | Expected Usage                                                                                                                                            | Status                  |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| Single Agent / Single Policy | The simplest possible RL implementation. Often used in toy examples.                                                                                      | Supported and Validated |
| Multi-Agent / Single Policy  | Crucial for multi-agent simulations (e.g. controlling 10 AGVs simultanously). <br/> Multi-agent RL is often necessary for real-life industrial use cases. | Supported and Validated |
| Multi-Agent / Multi-Policy   | We have not identified any real-life industrial use cases that require multiple RL policies simultanously.                                                | Unsupported             |
| Discrete Action Spaces       | Discrete choices such as an on/off switch.                                                                                                                | Supported and Validated |
| Continuous Action Spaces     | Continuous ranges such as a steering wheel.                                                                                                               | Supported and Validated |
| Tuple Action Spaces          | Multiple simultaneous actions such as driving a car which needs to stop/go and turn.                                                                      | Supported and Validated |
| Action Masking               | Prevent RL from taking illegal actions. Crucial for helping RL learn in use cases which contain many illegal actions.                                     | Supported and Validated |
| Auto-Regressive Actions      | Supported but we haven't found a good use case to validate this feature. Auto-regressive actions are often used in use cases such as robotics arms.       | Supported but Untested  |

## Technical Explanation (Optional)

NativeRL enables using environments for Reinforcement Learning implemented in Java or C++ with Python frameworks such as [Ray's RLlib](https://docs.ray.io/en/latest/rllib/index.html). You can think of it as a bridge between Java and Python.

It defines an intermediary C++ interface via the classes in `nativerl.h`, which are mapped to Java using JavaCPP and the classes in the `nativerl` submodule. The `nativerl::Environment` interface is meant to be subclassed by users to implement new environments, such as the ones outputted from code generated by `AnyLogicHelper.java`. The C++ classes are then made available to Python via pybind11 as per the bindings defined in `nativerl.cpp`, which one can use afterwards to implement environments as part of Python APIs, for example, OpenAI Gym or RLlib, as exemplified in code generated by `RLlibHelper.java`.
